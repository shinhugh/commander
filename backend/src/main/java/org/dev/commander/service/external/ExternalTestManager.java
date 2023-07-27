package org.dev.commander.service.external;

import org.dev.commander.model.Session;
import org.dev.commander.repository.SessionRepository;
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
        private final SessionRepository sessionRepository;

        public InnerInner(SessionRepository sessionRepository) {
            this.sessionRepository = sessionRepository;
        }

        public void test() {
            Session session = new Session();
            session.setToken("abc");
            session.setAccountId(1L);
            session.setAuthorities(1);
            session.setCreationTime(1L);
            session.setExpirationTime(2L);
            sessionRepository.save(session);
        }
    }
}
