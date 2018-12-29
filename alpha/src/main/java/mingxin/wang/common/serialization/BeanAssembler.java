package mingxin.wang.common.serialization;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
public final class BeanAssembler<T> {
    private final ImmutableMap<String, PropertySetter> propertySetters;

    private BeanAssembler(ImmutableMap<String, PropertySetter> propertySetters) {
        this.propertySetters = propertySetters;
    }

    public static <T> Builder<T> builder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    public void setProperty(T data, String propertyName, String value) throws NoSuchFieldException, InvocationTargetException {
        if (!hasProperty(propertyName)) {
            throw new NoSuchFieldException(propertyName);
        }
        PropertySetter propertySetter = propertySetters.get(propertyName);
        try {
            propertySetter.set(data, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Pre-requirements are not satisfied, illegal access to the field", e);
        }
    }

    public boolean hasProperty(String propertyName) {
        return propertySetters.containsKey(propertyName);
    }

    private interface PropertySetter {
        void set(Object data, String s) throws IllegalAccessException, InvocationTargetException;
    }

    public static final class Builder<T> {
        private Map<Class<?>, Function<? super String, ?>> typeConversions;
        private Map<String, Function<? super String, ?>> propertyConversions;
        private Class<T> clazz;

        private Builder(Class<T> clazz) {
            this.typeConversions = Maps.newHashMap();
            this.propertyConversions = Maps.newHashMap();
            this.clazz = clazz;
        }

        public <U> Builder<T> addTypeConversion(Class<U> clazz, Function<? super String, ? extends U> function) {
            typeConversions.put(clazz, function);
            return this;
        }

        public Builder<T> addPropertyConversion(String propertyName, Function<? super String, ?> function) {
            propertyConversions.put(propertyName, function);
            return this;
        }

        public BeanAssembler<T> build() {
            ImmutableMap.Builder<String, PropertySetter> propertySetterBuilder = ImmutableMap.builder();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                propertySetterBuilder.put(field.getName(), buildPropertySetter(field));
            }
            return new BeanAssembler<>(propertySetterBuilder.build());
        }

        private PropertySetter buildPropertySetter(Field field) {
            Class<?> clazz = field.getType();

            // If there is a custom conversion strategy, apply it
            Function<? super String, ?> function = getConversion(field);
            if (function != null) {
                return (data, s) -> {
                    Object result;
                    try {
                        result = function.apply(s);
                    } catch (Throwable e) {
                        throw new InvocationTargetException(e);
                    }
                    field.set(data, result);
                };
            }

            // For Strings, there is no need to reconstruct an object
            if (clazz.equals(String.class)) {
                return field::set;
            }

            // For primitive types, wrapping is necessary to construct an object from String
            if (clazz.isPrimitive()) {
                clazz = Primitives.wrap(clazz);
            }

            // If there is no constructor from String, do deserialization with the ObjectMapper;
            // Otherwise, call the directly.
            Constructor constructor = getStringConstructor(clazz);
            return constructor == null ? (data, s) -> {
                try {
                    field.set(data, Jsons.DEFAULT_OBJECT_MAPPER.readValue(s, field.getType()));
                } catch (IOException e1) {
                    throw new InvocationTargetException(e1);
                }
            } : (data, s) -> {
                try {
                    field.set(data, constructor.newInstance(s));
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException("Pre-requirements are not satisfied, instantiation failed due to incomplete type", e);
                }
            };
        }

        private Function<? super String, ?> getConversion(Field field) {
            Function<? super String, ?> result = propertyConversions.get(field.getName());
            return result == null ? typeConversions.get(field.getType()) : result;
        }

        private Constructor getStringConstructor(Class<?> clazz) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                return null;
            }
            Constructor result;
            try {
                result = clazz.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                return null;
            }
            return result;
        }
    }
}
