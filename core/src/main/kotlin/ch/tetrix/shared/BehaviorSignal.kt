package ch.tetrix.shared

import com.badlogic.ashley.signals.Signal

/** Signal, but it knows it's last emitted value */
class BehaviorSignal<T>(initialValue: T): Signal<T>() {
    val value get() = _value
    private var _value: T = initialValue

    override fun dispatch(newValue: T) {
        _value = newValue
        super.dispatch(newValue)
    }
}
