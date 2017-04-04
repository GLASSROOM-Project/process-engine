package de.glassroom.gpe.test;

public class GenericTest {
	public static void main(String[] args) {
		int cursor = 0;
		int size = 9;
		double out = (cursor + 1.0) / size;
		System.out.println((int) Math.ceil(out * 100));
	}
}
