package ru.basa62.wst.lab3.ws.exception;

import lombok.Getter;

import javax.xml.ws.WebFault;

@WebFault(faultBean = "ru.basa62.wst.lab3.ws.exception.BooksServiceFault")
public class UnuathorizedException extends Exception {

    @Getter
    private final BooksServiceFault faultInfo;

    public UnuathorizedException(String message, BooksServiceFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public UnuathorizedException(String message, Throwable cause, BooksServiceFault faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }
}
