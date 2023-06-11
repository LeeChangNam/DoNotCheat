/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.donotcheat.java.facedetector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;


import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.demo.CameraSourcePreview;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType;


import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;

import org.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
public class FaceGraphic extends Graphic {
  private static final float FACE_POSITION_RADIUS = 8.0f;
  private static final float ID_TEXT_SIZE = 30.0f;
  private static final float ID_Y_OFFSET = 40.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;
  private static final int NUM_COLORS = 10;

  private float limit;
  private float biggestY=0;//턱 밑의 점의 y좌표
  private float jawX=0;//턱 밑의 점의 x좌표
  private float cheekY=0;//턱 위쪽의 점의 y좌표
  private float lowestX=1000;//턱 위쪽의 점의 x좌표

  private Bitmap bm;

  private FirebaseStorage storage = FirebaseStorage.getInstance();

  private Jeongsu sendImages = new Jeongsu();

  private Window window;

  private ImageCapture imageCapture;
  private String combined;


  CameraSourcePreview preview;
  Intent intent;

  GraphicOverlay graphicOverlay;

  String examNum;
  String examineeNum;

  FirebaseFirestore db = FirebaseFirestore.getInstance();
  FirebaseFirestore errordb = FirebaseFirestore.getInstance();
  FirebaseFirestore cheatDb=FirebaseFirestore.getInstance();

  StorageReference storageRef= storage.getReference();


  private float standard= sendImages.makeStandard(60,30);

  private static final int[][] COLORS =
      new int[][] {
        // {Text color, background color}
        {Color.BLACK, Color.WHITE},
        {Color.WHITE, Color.MAGENTA},
        {Color.BLACK, Color.LTGRAY},
        {Color.WHITE, Color.RED},
        {Color.WHITE, Color.BLUE},
        {Color.WHITE, Color.DKGRAY},
        {Color.BLACK, Color.CYAN},
        {Color.BLACK, Color.YELLOW},
        {Color.WHITE, Color.BLACK},
        {Color.BLACK, Color.GREEN}
      };

  private final Paint facePositionPaint;
  private final Paint[] idPaints;
  private final Paint[] boxPaints;
  private final Paint[] labelPaints;

  private volatile Face face;

  FaceGraphic(GraphicOverlay overlay, Face face, Intent intent , GraphicOverlay graphicOverlay,CameraSourcePreview preview) {
    super(overlay);

    this.face = face;
    final int selectedColor = Color.WHITE;
    this.intent=intent;
    this.preview=preview;
    this.graphicOverlay=graphicOverlay;

    facePositionPaint = new Paint();
    facePositionPaint.setColor(selectedColor);

    int numColors = COLORS.length;
    idPaints = new Paint[numColors];
    boxPaints = new Paint[numColors];
    labelPaints = new Paint[numColors];
    for (int i = 0; i < numColors; i++) {
      idPaints[i] = new Paint();
      idPaints[i].setColor(COLORS[i][0] /* text color */);
      idPaints[i].setTextSize(ID_TEXT_SIZE);

      boxPaints[i] = new Paint();
      boxPaints[i].setColor(COLORS[i][1] /* background color */);
      boxPaints[i].setStyle(Paint.Style.STROKE);
      boxPaints[i].setStrokeWidth(BOX_STROKE_WIDTH);

      labelPaints[i] = new Paint();
      labelPaints[i].setColor(COLORS[i][1] /* background color */);
      labelPaints[i].setStyle(Paint.Style.FILL);
    }
  }

  /** Draws the face annotations for position on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    Face face = this.face;
    if (face == null) {
      return;
    }

    // Draws a circle at the position of the detected face, with the face's track id below.
    float x = translateX(face.getBoundingBox().centerX());
    float y = translateY(face.getBoundingBox().centerY());
    canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);

    // Calculate positions.
    float left = x - scale(face.getBoundingBox().width() / 2.0f);
    float top = y - scale(face.getBoundingBox().height() / 2.0f);
    float right = x + scale(face.getBoundingBox().width() / 2.0f);
    float bottom = y + scale(face.getBoundingBox().height() / 2.0f);
    float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
    float yLabelOffset = (face.getTrackingId() == null) ? 0 : -lineHeight;

    // Decide color based on face ID
    int colorID = (face.getTrackingId() == null) ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);

    // Calculate width and height of label box
    float textWidth = idPaints[colorID].measureText("ID: " + face.getTrackingId());
    if (face.getSmilingProbability() != null) {
      yLabelOffset -= lineHeight;
      textWidth =
          Math.max(
              textWidth,
              idPaints[colorID].measureText(
                  String.format(Locale.US, "Happiness: %.2f", face.getSmilingProbability())));
    }
    if (face.getLeftEyeOpenProbability() != null) {
      yLabelOffset -= lineHeight;
      textWidth =
          Math.max(
              textWidth,
              idPaints[colorID].measureText(
                  String.format(
                      Locale.US, "Left eye open: %.2f", face.getLeftEyeOpenProbability())));
    }
    if (face.getRightEyeOpenProbability() != null) {
      yLabelOffset -= lineHeight;
      textWidth =
          Math.max(
              textWidth,
              idPaints[colorID].measureText(
                  String.format(
                      Locale.US, "Right eye open: %.2f", face.getRightEyeOpenProbability())));
    }

    yLabelOffset = yLabelOffset - 3 * lineHeight;
    textWidth =
        Math.max(
            textWidth,
            idPaints[colorID].measureText(
                String.format(Locale.US, "EulerX: %.2f", face.getHeadEulerAngleX())));
    textWidth =
        Math.max(
            textWidth,
            idPaints[colorID].measureText(
                String.format(Locale.US, "EulerY: %.2f", face.getHeadEulerAngleY())));
    textWidth =
        Math.max(
            textWidth,
            idPaints[colorID].measureText(
                String.format(Locale.US, "EulerZ: %.2f", face.getHeadEulerAngleZ())));
    // Draw labels
    canvas.drawRect(
        left - BOX_STROKE_WIDTH,
        top + yLabelOffset,
        left + textWidth + (2 * BOX_STROKE_WIDTH),
        top,
        labelPaints[colorID]);
    yLabelOffset += ID_TEXT_SIZE;
    canvas.drawRect(left, top, right, bottom, boxPaints[colorID]);
    if (face.getTrackingId() != null) {
      canvas.drawText("ID: " + face.getTrackingId(), left, top + yLabelOffset, idPaints[colorID]);
      yLabelOffset += lineHeight;
    }


    System.out.println("Start");
    int ccccc=0;

    // Draws all face contours.
    for (FaceContour contour : face.getAllContours()) {
      for (PointF point : contour.getPoints()) {
        ccccc++;
        canvas.drawCircle(
            translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
        if(point.y>biggestY) {
          biggestY = point.y;
          jawX=point.x;
        }
        if(point.x<lowestX) {
          lowestX=point.x;
          cheekY=point.y;
        }


        System.out.println(point.x+", "+point.y);
      }

    }

    examNum=intent.getStringExtra("방번호");
    examineeNum=intent.getStringExtra("수험번호");

      db.collection("exam").document(examNum)
              .collection("userList").document(examineeNum)
              .get()
              .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                  if((Boolean)documentSnapshot.getData().get("start")==true)
                  {

                    HashMap<String,Object>limitX=new HashMap<>();
                    LocalDate now=LocalDate.now();
                    LocalTime nowC = LocalTime.now();
                    String dateStr = now.toString();
                    String timeStr = nowC.toString();
                    combined = dateStr + "_" + timeStr.substring(0,8);
                    String root=examNum+"/"+examineeNum;
                    String filename = combined+"_default"+".png";
                    limit=sendImages.limitX(lowestX,jawX,standard);
                    limitX.put("default",(long)(limit));
                    limitX.put("start",(Boolean)false);
                    limitX.put("imageName",(String)filename);
                    limitX.put("examineeName",(String)documentSnapshot.getData().get("examineeName"));
                    cheatDb.collection("exam").document(examNum).collection("userList").document(examineeNum)
                            .set(limitX);

//                        File screenShot = ScreenShot(preview,filename,root);
                    graphicOverlay.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

                    Bitmap screenBitmap = graphicOverlay.getDrawingCache();   //캐시를 비트맵으로 변환
                    uploadBitmapToFirebaseStorage(screenBitmap,filename,root);
                  }
                  else
                  {
                    if((limit=(Long)documentSnapshot.getData().get("default"))>0) {
                      Jeongsu sendImage = new Jeongsu();
                      if(sendImage.checkCheating(limit,jawX,lowestX)) {

                        LocalDate now=LocalDate.now();
                        LocalTime nowC = LocalTime.now();
                        String dateStr = now.toString();
                        String timeStr = nowC.toString();
                        combined = dateStr + "_" + timeStr.substring(0,8);
                        HashMap<String,Object>error=new HashMap<>();
                        error.put("error_value",limit-(jawX-lowestX));
                        error.put("cheatTime",combined);
                        errordb.collection("exam").document(examNum).collection("userList").document(examineeNum)
                                .collection("cheatList").document(combined).set(error);
                        String root=examNum+"/"+examineeNum;
                        String filename = combined+".png";
//                        File screenShot = ScreenShot(preview,filename,root);
                        graphicOverlay.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

                        Bitmap screenBitmap = graphicOverlay.getDrawingCache();   //캐시를 비트맵으로 변환

                        uploadBitmapToFirebaseStorage(screenBitmap,filename,root);
//                        graphicOverlay.destroyDrawingCache();
//
//// 옵션으로 캐시를 완전히 제거
//                        graphicOverlay.setDrawingCacheEnabled(false);
                        //Uri fileUri = Uri.fromFile(screenShot);
//                        StorageReference riverRef=storageRef.child(root);
//                        UploadTask uploadTask=riverRef.putFile(fileUri);

                      }
                    }
                  }
                }
              });


    System.out.println(ccccc+"개\nEnd");

    //파이어스토어에 추가하는 코드
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    Map<String, Object> cheating = new HashMap<>();
//    cheating.put("cheatTime", "2023.05.13");
//    cheating.put("imageName", "ewwfwfwe");
//    cheating.put("length", 5);

//    db.collection("exam").document("00000002").collection("userList").document("20192444").collection("cheatList").document("test").set(cheating);


    // Draws smiling and left/right eye open probabilities.
    if (face.getSmilingProbability() != null) {
      canvas.drawText(
          "Smiling: " + String.format(Locale.US, "%.2f", face.getSmilingProbability()),
          left,
          top + yLabelOffset,
          idPaints[colorID]);
      yLabelOffset += lineHeight;
    }

    FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
    if (face.getLeftEyeOpenProbability() != null) {
      canvas.drawText(
          "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
          left,
          top + yLabelOffset,
          idPaints[colorID]);
      yLabelOffset += lineHeight;
    }
    if (leftEye != null) {
      float leftEyeLeft =
          translateX(leftEye.getPosition().x) - idPaints[colorID].measureText("Left Eye") / 2.0f;
      canvas.drawRect(
          leftEyeLeft - BOX_STROKE_WIDTH,
          translateY(leftEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
          leftEyeLeft + idPaints[colorID].measureText("Left Eye") + BOX_STROKE_WIDTH,
          translateY(leftEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
          labelPaints[colorID]);
      canvas.drawText(
          "Left Eye",
          leftEyeLeft,
          translateY(leftEye.getPosition().y) + ID_Y_OFFSET,
          idPaints[colorID]);
    }

    FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
    if (face.getRightEyeOpenProbability() != null) {
      canvas.drawText(
          "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
          left,
          top + yLabelOffset,
          idPaints[colorID]);
      yLabelOffset += lineHeight;
    }
    if (rightEye != null) {
      float rightEyeLeft =
          translateX(rightEye.getPosition().x) - idPaints[colorID].measureText("Right Eye") / 2.0f;
      canvas.drawRect(
          rightEyeLeft - BOX_STROKE_WIDTH,
          translateY(rightEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
          rightEyeLeft + idPaints[colorID].measureText("Right Eye") + BOX_STROKE_WIDTH,
          translateY(rightEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
          labelPaints[colorID]);
      canvas.drawText(
          "Right Eye",
          rightEyeLeft,
          translateY(rightEye.getPosition().y) + ID_Y_OFFSET,
          idPaints[colorID]);
    }




    canvas.drawText(
        "EulerX: " + face.getHeadEulerAngleX(), left, top + yLabelOffset, idPaints[colorID]);
    yLabelOffset += lineHeight;
    canvas.drawText(
        "EulerY: " + face.getHeadEulerAngleY(), left, top + yLabelOffset, idPaints[colorID]);
    yLabelOffset += lineHeight;
    canvas.drawText(
        "EulerZ: " + face.getHeadEulerAngleZ(), left, top + yLabelOffset, idPaints[colorID]);

    // Draw facial landmarks
    drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE);
    drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE);
    drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK);
    drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK);
  }

  private void drawFaceLandmark(Canvas canvas, @LandmarkType int landmarkType) {
    FaceLandmark faceLandmark = face.getLandmark(landmarkType);
    if (faceLandmark != null) {
      canvas.drawCircle(
          translateX(faceLandmark.getPosition().x),
          translateY(faceLandmark.getPosition().y),
          FACE_POSITION_RADIUS,
          facePositionPaint);
    }
  }
//  public File ScreenShot(CameraSourcePreview view,String filename,String root){
//    view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다
//
//    Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환
//
//    File file = new File("/sdcard/Pictures", filename);  //Pictures폴더 screenshot.png 파일
//    FileOutputStream os = null;
//    try{
//      os = new FileOutputStream(file);
//      screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
//      os.close();
//    }catch (IOException e){
//      e.printStackTrace();
//      return null;
//    }
//
//    view.setDrawingCacheEnabled(false);
//    return file;
//  }

  public void uploadBitmapToFirebaseStorage(Bitmap bitmap, String filename, String root) {
    // Bitmap을 ByteArrayOutputStream에 압축
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
    byte[] data = baos.toByteArray();


    // Firebase Storage 인스턴스 가져오기
    FirebaseStorage storage = FirebaseStorage.getInstance();

    // 저장될 파일 경로와 이름 설정
    //String storagePath = root + filename;

    // 이미지를 저장할 스토리지 레퍼런스 생성
    StorageReference storageRef = storage.getReference();
    StorageReference imageRef = storageRef.child(examNum).child(examineeNum).child(filename);

    // 이미지 업로드
    UploadTask uploadTask = imageRef.putBytes(data);
    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        // 이미지 업로드 성공
        // taskSnapshot.getMetadata().getReference().getDownloadUrl()를 사용하여 이미지의 다운로드 URL을 가져올 수 있습니다.
//        Uri downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();
        // 필요에 따라 이미지 URL을 사용하여 다른 작업을 수행할 수 있습니다.
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        // 이미지 업로드 실패
        e.printStackTrace();
      }
    });
  }




}
