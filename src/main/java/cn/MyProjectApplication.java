package cn;

import org.springframework.boot.SpringApplication;

@MyAppConfig
public class MyProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyProjectApplication.class, args);
		System.out.println("启动成功");
	}

}
