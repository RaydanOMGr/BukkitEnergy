package com.burchard36.bukkit.capability;

import com.burchard36.bukkit.energy.IEnergyStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Simple reflection utility for {@link EnergyFactory} to initiate new objects of its generic IEnergyStorage to blocks
 * @param <SomeEnergyImpl> a class extending {@link IEnergyStorage}
 */
public class SimpleReflection<SomeEnergyImpl extends IEnergyStorage> {

    private final Class<SomeEnergyImpl> clazz;

    public SimpleReflection(final Class<SomeEnergyImpl> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public SomeEnergyImpl newInstance(final Object o) {
        Constructor<?>[] constructors = this.clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
            if (constructorParameterTypes.length == 0) throw new RuntimeException("Class %s did not provider a valid constructor for a IEnergyStorage type. Smh.".formatted(this.clazz.getName()));
            try {
                return (SomeEnergyImpl) constructor.newInstance(o);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error with class %s ".formatted(this.clazz.getName()) + e);
            }
        }

        throw new RuntimeException("Class %s did not have a constructor to initiate!".formatted(this.clazz.getName()));
    }

}
