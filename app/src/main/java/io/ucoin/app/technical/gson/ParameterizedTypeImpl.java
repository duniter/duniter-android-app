package io.ucoin.app.technical.gson;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type rawType;
    private final Type ownerType;
    private final Type[] typeArguments;

    public ParameterizedTypeImpl(final Type rawType, final Type ownerType, final Type... typeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.typeArguments = typeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

}