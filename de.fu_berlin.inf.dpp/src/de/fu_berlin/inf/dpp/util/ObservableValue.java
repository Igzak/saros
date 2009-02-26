/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;
import java.util.Set;

/**
 * An ObservableValue is like a normal variable (you can get and set its value),
 * but also allows listeners to register for changes.
 * 
 * @author oezbek
 */
public class ObservableValue<T> {

    Set<ValueChangeListener<? super T>> nonVetoables = new HashSet<ValueChangeListener<? super T>>();

    T variable;

    /**
     * Create a new ObservableValue with the given initial value.
     */
    public ObservableValue(T initialValue) {
        super();
        this.variable = initialValue;
    }

    /**
     * Utility function which adds the given Listener and directly afterwards
     * calls the listener with the current value.
     */
    public void addAndNotify(ValueChangeListener<? super T> listener) {
        nonVetoables.add(listener);
        listener.setValue(variable);
    }

    /**
     * Register as a listener with this Observable value.
     * 
     * The listener will be called each time the
     * {@link ObservableValue#setValue(Object)}
     * 
     */
    public void add(ValueChangeListener<? super T> listener) {
        nonVetoables.add(listener);
    }

    public void remove(ValueChangeListener<? super T> listener) {
        nonVetoables.remove(listener);
    }

    /**
     * 
     * Sets a new value for this ObservableValue and notify all listeners.
     * 
     * It is safe to call {@link ObservableValue#getValue()} to get the current
     * value of the ObservableValue from the listener.
     * 
     * Note: The listeners are called, even if the newValue is not changed! Thus
     * calling
     * 
     * setValue(getValue())
     * 
     * would trigger a call to all listeners.
     * 
     * @return Returns true if the setting of the variable was successful (this
     *         is used for a {@link VetoableObservableValue}).
     * 
     */
    public boolean setValue(T newValue) {
        return setValue(newValue, null);
    }

    /**
     * Utility function for {@link ObservableValue#setValue(Object)} which
     * excludes the given listener to be notified of the change.
     * 
     * This is useful, when the change originated from a component which does
     * not need to be updated again (to avoid a cycle).
     * 
     */
    public boolean setValue(T newValue, ValueChangeListener<? super T> exclude) {
        this.variable = newValue;
        for (ValueChangeListener<? super T> vpl : nonVetoables) {
            if (vpl == exclude)
                continue;
            vpl.setValue(newValue);
        }
        return true;
    }

    /**
     * 
     * @return
     */
    public T getValue() {
        return variable;
    }
}
