package de.glassroom.gpe;

public interface Filter<E> {
    public boolean accept(E obj);
}
