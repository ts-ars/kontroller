// 📦 common.domain
package com.exempal.shiftcounter.common.domain;

public interface EventPublisherPort {
    void publish(Object event);
}
