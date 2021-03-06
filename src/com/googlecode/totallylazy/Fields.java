package com.googlecode.totallylazy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.googlecode.totallylazy.Classes.allClasses;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class Fields {
    public static Mapper<Field, String> name = new Mapper<Field, String>() {
        @Override
        public String call(Field field) throws Exception {
            return field.getName();
        }
    };

    public static Mapper<Field, Class<?>> type = new Mapper<Field, Class<?>>() {
        @Override
        public Class<?> call(Field field) throws Exception {
            return field.getType();
        }
    };

    public static <T> Mapper<Field, T> value(final Object instance) {
        return new Mapper<Field, T>() {
            @Override
            public T call(Field field) throws Exception {
                return get(field, instance);
            }
        };
    }

    public static <T> Mapper<Field, T> value(final Object instance, final Class<T> aClass) {
        return new Mapper<Field, T>() {
            @Override
            public T call(Field field) throws Exception {
                return get(field, instance, aClass);
            }
        };
    }

    public static <T> T get(Field field, Object instance) throws IllegalAccessException {
        return cast(access(field).get(instance));
    }

    public static <T> T get(Field field, Object instance, Class<T> aClass) throws IllegalAccessException {
        return aClass.cast(access(field).get(instance));
    }

    public static Mapper<Field, Integer> modifiers = new Mapper<Field, Integer>() {
        @Override
        public Integer call(Field field) throws Exception {
            return field.getModifiers();
        }
    };

    public static Field access(Field field) {
        field.setAccessible(true);
        return field;
    }

    public static Sequence<Field> fields(Class<?> aClass) {
        return allClasses(aClass).flatMap(Fields.fields());
    }

    public static Mapper<Class<?>, Sequence<Field>> fields() {
        return new Mapper<Class<?>, Sequence<Field>>() {
            public Sequence<Field> call(Class<?> aClass) throws Exception {
                return sequence(aClass.getDeclaredFields());
            }
        };
    }

    public static Sequence<Field> syntheticFields(Class<?> aClass) {
        return sequence(aClass.getDeclaredFields()).
                filter(where(modifiers, is(Reflection.synthetic)));
    }

    public static Sequence<Field> nonSyntheticFields(Class<?> aClass) {
        return sequence(aClass.getDeclaredFields()).
                filter(where(modifiers, not(Reflection.synthetic)));
    }
}
