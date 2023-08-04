package org.dev.pixels.service.external;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalTestManager implements ExternalTestService {
    private final Inner inner;

    public ExternalTestManager(Inner inner) {
        this.inner = inner;
    }

    @Override
    public void test() {
        inner.test();
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static class Inner {
        private final InnerInner innerInner;

        public Inner(InnerInner innerInner) {
            this.innerInner = innerInner;
        }

        public void test() {
            innerInner.test();
        }
    }

    @Component
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static class InnerInner {
        public void test() {
            // TEST
        }
    }
}
