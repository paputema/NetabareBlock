package com.netabareblock;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.netabareblock.data.NetabareAccountData;
import com.netabareblock.data.UserAccountData;
import com.netabareblock.repositories.NetabareAccountDataRepository;
import com.netabareblock.repositories.UserAccountDataRepository;

import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Component
@EnableScheduling
public class NetabareBlockCron {
	@Autowired
	UserAccountDataRepository userAccountDataRepository;

	@Autowired
	NetabareAccountDataRepository netabareAccountDataRepository;

	@Autowired
	NetabareBlockConfig netabareBlockConfig;

	static ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	/**
	 * @author paputema
	 *
	 */
	class NetabareBlockTodo extends Thread {


		private Twitter twitter;
		private UserAccountData userData;


		@Override
		public void run() {
			super.run();
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(netabareBlockConfig.getConsumerKey());
			builder.setOAuthConsumerSecret(netabareBlockConfig.getConsumerSecret());
			builder.setOAuthAccessToken(userData.getAccessToken());
			builder.setOAuthAccessTokenSecret(userData.getAccessTokenSecret());
			twitter = new TwitterFactory(builder.build()).getInstance();

			for (NetabareAccountData netabareAccountData : netabareAccountDataRepository.findAll()) {
				try{
					Long netabareid = netabareAccountData.getUserid();
					Relationship relationship = twitter.showFriendship(twitter.getId(), netabareid);
					if(!relationship.isSourceBlockingTarget())
					{
						checkRateLimit(twitter.reportSpam(netabareid).getRateLimitStatus());
						checkRateLimit(twitter.createBlock(netabareid).getRateLimitStatus());
					}
				} catch (TwitterException e) {
					System.err.println(e);
				}
			}
		}



		public NetabareBlockTodo(UserAccountData userData) {
			this.userData = userData;
		}

		private void checkRateLimit(RateLimitStatus rateLimitStatus) {
			if (rateLimitStatus != null && rateLimitStatus.getRemaining() <= 0) {
				System.out.println(rateLimitStatus);
				long time = rateLimitStatus.getSecondsUntilReset();
				exec.setCorePoolSize(exec.getCorePoolSize() + 1);
				try {
					if (time < 0) {
						time += 120;
					}
					Thread.sleep(time * 1000);
					if (exec.getCorePoolSize() > 1) {
						exec.setCorePoolSize(exec.getCorePoolSize() - 1);
					}
				} catch (InterruptedException | IllegalArgumentException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}

	}

	@Scheduled(cron = "0 0 0,4,8,12,16,20 * * *", zone = "Asia/Tokyo")
	public void todo() {
		List<UserAccountData> userDatas = userAccountDataRepository.findAll();
		for (UserAccountData userData : userDatas) {
			exec.execute(new NetabareBlockTodo(userData));
		}
	}


}
