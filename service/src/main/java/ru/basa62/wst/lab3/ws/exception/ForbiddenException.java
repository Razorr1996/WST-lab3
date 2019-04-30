package ru.basa62.wst.lab3.ws.exception;

import lombok.Getter;

import javax.xml.ws.WebFault;

@WebFault(faultBean = "ru.basa62.wst.lab3.ws.exception.BooksServiceFault")
public class ForbiddenException extends Exception {

    @Getter
    private final BooksServiceFault faultInfo;

    public ForbiddenException(String message, BooksServiceFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public ForbiddenException(String message, Throwable cause, BooksServiceFault faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }
}
