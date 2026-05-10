package ru.tikonovns.capstone.spring.exception;

/*
Используется, когда объект не найден
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}