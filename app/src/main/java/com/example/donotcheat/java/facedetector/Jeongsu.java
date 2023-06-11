package com.example.donotcheat.java.facedetector;

import android.graphics.PointF;

public class Jeongsu {

    //d는 사용자와 컴퓨터 화면과의 거리 , x는 화면의 가로 길이
    public float makeStandard(float d,float x){
        float standard;
        standard=(float)Math.atan(x/(2*d));
        return standard;
    }
    //입력값으로 첫 시작의 두 좌표를 입력한다(cheek이 윗부분,jaw가 턱밑부분)
    //x좌표의 한계 좌표를 구한다. 여기서 리턴한 x좌표보다 더 x좌표가 작아질 경우에 치팅 발동!
    public float limitX(float cheekx,float jawx, float standard){

        float limit;
        float dif;
        dif=jawx-cheekx;
        if(dif<0)
            dif=dif*(-1);
        limit=jawx-cheekx-(dif/90*standard);
        return limit;
    }


    //cheating여부를 판단해주는 함수 cheating했을시 1, 아니면 0을 리턴한다.
    public boolean checkCheating(float limitX,float jawX,float cheekX){
        float length=jawX-cheekX;
        if(length<0)
            length=length*(-1);

        float errorValue= limitX-length;
        if(errorValue<0)
            errorValue=errorValue*(-1);
//        if(length<limitX) {
//            return true;
//        }
//        else
//            return false;

        if(errorValue>8)
            return true;
        else
            return false;
    }

}
