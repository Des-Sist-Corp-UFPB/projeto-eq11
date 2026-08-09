package br.ufpb.dsc.studyai;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashTest {
    @Test
    public void printHash() {
        System.out.println("HASH_ADMIN: " + new BCryptPasswordEncoder().encode("admin123"));
    }
}
