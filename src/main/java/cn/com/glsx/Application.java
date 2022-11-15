package cn.com.glsx;

import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.context.EnableRestAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;

/**
 * @author payu
 */
@Slf4j
@EnableRestAdmin
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@SpringBootApplication(scanBasePackages = {"cn.com.glsx.*"})
public class Application {

    public static void main(String[] args) {
        log.info("start begin...{}", DateUtils.currentDateTime());
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.publishEvent(new StartoverEvent(context));
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(getTimeZone(of("Asia/Shanghai")));
        log.info("started...{}", DateUtils.currentDateTime());
    }

}

@Slf4j
class StartoverEvent extends ApplicationContextEvent {

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source the {@code ApplicationContext} that the event is raised for
     *               (must not be {@code null})
     */
    public StartoverEvent(ApplicationContext source) {
        super(source);
        log.info("start over...{}", DateUtils.currentDateTime());
    }

}