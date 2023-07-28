package org.dev.commander.service.external;

import org.dev.commander.repository.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        private final SessionRepository sessionRepository;

        public InnerInner(SessionRepository sessionRepository) {
            this.sessionRepository = sessionRepository;
        }

        public void test() {
            try {
                List<Integer> list = List.of(1, 2, 1);
                Set<Integer> set = new HashSet<>(list);
                for (int element : set) {
                    System.out.println("@@ " + element);
                }
            }
            catch (Exception ex) {
                System.out.println("@@ " + ex.getClass());
            }
        }
    }
}
