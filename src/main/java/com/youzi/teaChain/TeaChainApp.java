package com.youzi.teaChain;

import com.youzi.teaChain.service.TimeScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TeaChainApp implements EmbeddedServletContainerCustomizer
{
	private static final Logger logger = LoggerFactory.getLogger(TeaChainApp.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TeaChainApp.class);
//		app.setWebEnvironment(false);
        app.addListeners(new ApplicationInitializer());
		ApplicationContext ctx = app.run(args);

        runService(ctx);
	}

    private static void runService(ApplicationContext ctx) {
        TimeScheduledService timeScheduledService = (TimeScheduledService)ctx.getBean(TimeScheduledService.class);

        new Thread() {
            @Override
            public void run() {
                timeScheduledService.start();
            }
        }.start();
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        //指定项目名称
//        container.setContextPath("/demo");
        //指定端口地址
//        container.setPort(8050);
//        container.setPort(8085);
        container.setPort(80);
    }

}
