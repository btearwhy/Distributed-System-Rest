package be.kuleuven.foodrestservice.exceptions;

import org.springframework.http.HttpStatus;

public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(String id) {
        super(HttpStatus.NOT_FOUND + " Could not find meal " + id);
    }
}
