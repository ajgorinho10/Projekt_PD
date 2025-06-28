package projekt.PD;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import projekt.PD.Util.TotpUtil;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TotpUtilTest {

    // Wektory testowe z RFC 6238, Appendix B.
    // Sekret: "12345678901234567890" w ASCII
    private final String rfcSecret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    @DisplayName("Powinien wygenerować losowy, 32-znakowy sekret Base32")
    void shouldGenerateRandomSecret() {
        // When
        String secret = TotpUtil.generateSecret();

        // Then
        assertThat(secret).isNotNull();
        assertThat(secret.length()).isEqualTo(32);
        assertThat(secret).matches(Pattern.compile("[A-Z2-7]+"));
    }

    @Test
    @DisplayName("Powinien poprawnie zweryfikować kod dla bieżącego kroku czasowego")
    void shouldVerifyCodeForCurrentTimeStep() {
        // Given
        String secret = TotpUtil.generateSecret();
        long currentTime = Instant.now().toEpochMilli();
        long currentTimeStep = currentTime / TimeUnit.SECONDS.toMillis(30);
        String code = TotpUtil.generateCode(secret, currentTimeStep);

        // When & Then
        assertThat(TotpUtil.verifyCode(secret, code)).isTrue();
    }

    @Test
    @DisplayName("Powinien poprawnie zweryfikować kod dla poprzedniego kroku czasowego (rozbieżność czasu)")
    void shouldVerifyCodeForPreviousTimeStep() {
        // Given
        String secret = TotpUtil.generateSecret();
        long currentTime = Instant.now().toEpochMilli();
        long previousTimeStep = (currentTime / TimeUnit.SECONDS.toMillis(30)) - 1;
        String code = TotpUtil.generateCode(secret, previousTimeStep);

        // When & Then
        assertThat(TotpUtil.verifyCode(secret, code)).isTrue();
    }

    @Test
    @DisplayName("Powinien odrzucić kod, który jest zbyt stary")
    void shouldRejectCodeThatIsTooOld() {
        // Given
        String secret = TotpUtil.generateSecret();
        long currentTime = Instant.now().toEpochMilli();
        // 2 kroki w przeszłość = 60 sekund
        long oldTimeStep = (currentTime / TimeUnit.SECONDS.toMillis(30)) - 2;
        String code = TotpUtil.generateCode(secret, oldTimeStep);

        // When & Then
        assertThat(TotpUtil.verifyCode(secret, code)).isFalse();
    }

    @Test
    @DisplayName("Powinien odrzucić niepoprawny kod")
    void shouldRejectInvalidCode() {
        // Given
        String secret = TotpUtil.generateSecret();
        long currentTime = Instant.now().toEpochMilli();

        // When & Then
        assertThat(TotpUtil.verifyCode(secret, "000000")).isFalse();
    }

    @Test
    @DisplayName("Powinien wygenerować poprawny URI dla Google Authenticator")
    void shouldGenerateCorrectTotpUri() {
        // Given
        String issuer = "Moja Aplikacja";
        String username = "user@example.com";
        String secret = "JBSWY3DPEHPK3PXP"; // Przykładowy sekret

        // When
        String uri = TotpUtil.generateTotpUri(issuer, username, secret);

        // Then
        String expectedUri = "otpauth://totp/Moja%20Aplikacja:user%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=Moja%20Aplikacja&algorithm=SHA1&digits=6&period=30";
        assertThat(uri).isEqualTo(expectedUri);
    }

    @Test
    @DisplayName("Powinien wygenerować kod QR jako Base64 PNG")
    void shouldGenerateQrCodeAsBase64Png() {
        // Given
        String uri = "otpauth://totp/Test:test@test.com?secret=SECRET&issuer=Test";

        // When
        String qrCode = TotpUtil.generateQrCode(uri);

        // Then
        assertThat(qrCode).startsWith("data:image/png;base64,");
        assertThat(qrCode.substring("data:image/png;base64,".length())).isBase64();
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek podczas generowania QR dla niepoprawnego URI")
    void shouldThrowExceptionWhenGeneratingQrForInvalidUri() {
        // Given
        // Tworzymy bardzo długi URI, który przekroczy pojemność kodu QR
        String longUri = "a".repeat(5000);

        // When & Then
        assertThatThrownBy(() -> TotpUtil.generateQrCode(longUri))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Błąd podczas generowania kodu QR");
    }
}