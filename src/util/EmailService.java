package util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


public class EmailService {
    // 발송용 구글 계정 정보 (본인의 정보를 입력하세요)
    private final String user = "wowls8851@gmail.com"; 
    private final String password = "idaxlybnjasqkics"; 

    /**
     * 임시 비밀번호를 사용자의 이메일로 발송합니다.
     * @param toEmail 수신자 이메일
     * @param tempPassword 발송할 임시 비밀번호 (평문)
     */
    public void sendTempPassword(String toEmail, String tempPassword) {
        // 1. SMTP 서버 설정
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // 2. 세션 생성 및 인증
        Session session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // 3. 메시지 작성
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            
            message.setSubject("[True Oil] 임시 비밀번호가 발송되었습니다."); // 제목
            
            // 본문 내용 (HTML 형식)
            String content = "<h3>안녕하세요, True Oil입니다.</h3>"
                           + "<p>요청하신 임시 비밀번호는 다음과 같습니다.</p>"
                           + "<h2 style='color:blue;'>" + tempPassword + "</h2>"
                           + "<p>로그인 후 마이페이지에서 반드시 비밀번호를 변경해 주세요.</p>";
            
            message.setContent(content, "text/html; charset=utf-8");

            // 4. 메일 발송
            Transport.send(message);
            System.out.println("[EmailService] " + toEmail + "로 메일 발송 성공!");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("[EmailService] 메일 발송 중 오류 발생: " + e.getMessage());
        }
    }
}