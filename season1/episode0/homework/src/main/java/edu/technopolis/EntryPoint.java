package edu.technopolis;

import homework_1.FibonacciNumbers;

/**
 * Это просто точка входа для первого домашнего задания.
 * Содержит только метод main и ничего кроме. Никакой дполнительной нагрузки не несёт.
 * Является интерфейсом для
 * <ul>
 *     <li>Демонстрации возможности метода main в интрефейсе</li>
 *     <li>Экономии слова public</li>
 * </ul>
 */
public interface EntryPoint {
    static void main(String... args) {
        FibonacciNumbers algorithm = new FibonacciNumbers();
        System.out.println(algorithm.evaluate(Integer.parseInt(args[0])));
    }
}
