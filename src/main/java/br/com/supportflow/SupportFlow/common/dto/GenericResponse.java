package br.com.supportflow.SupportFlow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GenericResponse {
    public String message;


    public GenericResponse(String _message){
        this.message = _message;
    }
}
