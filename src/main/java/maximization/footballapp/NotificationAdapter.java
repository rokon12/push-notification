package maximization.footballapp;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * @author Bazlur Rahman Rokon
 * @since 6/2/16.
 */
public class NotificationAdapter {
    private static final Logger log = LoggerFactory.getLogger(NotificationAdapter.class);

    public static void main(String[] args) throws IOException, URISyntaxException {

        ClassLoader classLoader = NotificationAdapter.class.getClassLoader();

        URL resource = classLoader.getResource("dev_cert.p12");
        File file = Paths.get(resource.toURI()).toFile();


        final ApnsClient<SimpleApnsPushNotification> apnsClient = new ApnsClient<>(
                file, "football007");

        final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
        try {
            connectFuture.await();

            final SimpleApnsPushNotification pushNotification;
            {
                final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
                payloadBuilder.setAlertBody("Example!");

                final String payload = payloadBuilder.buildWithDefaultMaximumLength();
                final String token = TokenUtil.sanitizeTokenString("8ac3e28d761b74eff6fea8b1f2e05c55ab7539c45710df5c382c843889217883");


                //TODO: figure out what is topic
                pushNotification = new SimpleApnsPushNotification(token, "maximization.footballapp.NotificationAdapter", payload);
                final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                        apnsClient.sendNotification(pushNotification);

                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                        sendNotificationFuture.get();

                if (pushNotificationResponse.isAccepted()) {
                    log.info("Push notification accepted by APNs gateway.");
                } else {
                    log.info("Notification rejected by the APNs gateway: {}", pushNotificationResponse.getRejectionReason());
                    if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                        log.info("\t…and the token is invalid as of {}", pushNotificationResponse.getTokenInvalidationTimestamp());
                    }
                }
            }
            final Future<Void> disconnectFuture = apnsClient.disconnect();
            disconnectFuture.await();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
