package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import java.util.List;

public class UnzerErrorsDTO {
    private String id;

    private boolean isSuccess;

    private boolean isPending;

    private boolean isError;

    private String url;

    private String timestamp;

    private String traceId;

    private List<Errors> errors;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setIsSuccess(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
    public boolean getIsSuccess(){
        return this.isSuccess;
    }
    public void setIsPending(boolean isPending){
        this.isPending = isPending;
    }
    public boolean getIsPending(){
        return this.isPending;
    }
    public void setIsError(boolean isError){
        this.isError = isError;
    }
    public boolean getIsError(){
        return this.isError;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getUrl(){
        return this.url;
    }
    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }
    public String getTimestamp(){
        return this.timestamp;
    }
    public void setTraceId(String traceId){
        this.traceId = traceId;
    }
    public String getTraceId(){
        return this.traceId;
    }
    public void setErrors(List<Errors> errors){
        this.errors = errors;
    }
    public List<Errors> getErrors(){
        return this.errors;
    }
}
