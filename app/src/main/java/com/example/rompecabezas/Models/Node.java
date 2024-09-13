package com.example.rompecabezas.Models;
public class Node {
   public Fichas[][] data; // Datos del nodo
   public Node next; // Referencia al siguiente nodo

    // Constructor
    public Node(Fichas[][] data) {
        this.data = data;
        this.next = null;
    }

    public Fichas[][] getData() {
        return data;
    }

    public void setData(Fichas[][] data) {
        this.data = data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
    public int calcularH1(){
        int h1=0;
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                if(data[i][j].getId()!=(i*data[0].length+j+1)){
                    h1++;
                }
            }
        }
        return h1;
    }
    public int calcularH4(){
        int h4=0;
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                h4=h4+lugar(i,j,data[i][j].getId());
            }
        }
        return h4;
    }
    public int lugarN(int ii,int jj, int n){
        int ji=0,ij=0;
        int h2=0;
        for(int i=0;i<data.length;i++) {
            for (int j = 0; j < data[0].length; j++) {
                if((i*data[0].length +j+1)==n){
                    ji=i;
                    ij=j;
                    break;
                }
            }
        }

        if(ii==ji && jj==ij){
            h2=h2-10;
        }else{
            if(ii==ji){
                h2=h2-5;
            }
            if(jj==ij){
                h2=h2-5;
            }
        }

        return h2;
    }
    public int lugar(int ii,int jj, int n){
        int ji=0,ij=0;
        for(int i=0;i<data.length;i++) {
            for (int j = 0; j < data[0].length; j++) {
                if((i*data[0].length +j+1)==n){
                    ji=i;
                    ij=j;
                    break;
                }
            }
        }
        int resi=ii-ji;
        int resj=jj-ij;
        return   ((resi<0)?resi*(-1):resi) + ((resj<0)?resj*(-1):resj);
    }
    public int calcularH2(){
        int h2=0;
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                h2=h2+lugar(i,j,data[i][j].getId());
            }
        }
        return h2;
    }
    public int calcularH3(){

        return (int) Math.floor(calcularH1()+calcularH2()*0.5) ;
    }
}