package ch.tetrix.shared

import com.badlogic.ashley.signals.Signal

/**
 * A type-safe reactive class that holds a value of type `T` and allows observers
 * to be notified of updates to this value. It extends the `Signal` class.
 *
 * @param T The type of the value being observed and managed.
 * @property value The current value of type `T` that is being observed.
 * It is updated whenever `dispatch` is called with a new value.
 * @constructor Initializes the signal with an initial value of type `T`.
 */
class BehaviorSignal<T>(initialValue: T): Signal<T>() {
    val value get() = _value
    private var _value: T = initialValue

    override fun dispatch(newValue: T) {
        _value = newValue
        super.dispatch(newValue)
    }
}
