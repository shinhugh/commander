package org.dev.commander.service.external;

import jakarta.transaction.Transactional;
import org.dev.commander.model.Session;
import org.dev.commander.repository.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
    @Transactional
    public static class Inner {
        private final SessionRepository sessionRepository;

        public Inner(SessionRepository sessionRepository) {
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
