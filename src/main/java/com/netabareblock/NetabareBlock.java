package com.netabareblock;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.netabareblock.data.NetabareAccountData;
import com.netabareblock.data.UserAccountData;
import com.netabareblock.repositories.NetabareAccountDataRepository;
import com.netabareblock.repositories.UserAccountDataRepository;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Controller
public class NetabareBlock {
	@Autowired
	private HttpSession session;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	UserAccountDataRepository userAccountDataRepository;

	@Autowired
	NetabareAccountDataRepository netabareAccountDataRepository;

	@Autowired
	NetabareBlockConfig netabareBlockConfig;


	private void addObjectBySession(ModelAndView mav,String attName,Object defobject)
	{
		Object object = session.getAttribute(attName);
		mav.addObject(attName,(object != null) ? object : defobject);
		session.removeAttribute(attName);
	}

	@RequestMapping("/")
	public ModelAndView index(ModelAndView mav) {

		addObjectBySession(mav,"ret","ブロック対象ID");
		addObjectBySession(mav,"listNetabareAccount",netabareAccountDataRepository.findAll());
		addObjectBySession(mav,"niconico",false);
		mav.setViewName("index");

		return mav;
	}


	@RequestMapping("requestToken")
	public ModelAndView requestToken(ModelAndView mav) {
		try {
			mav.addObject("force_login", true);
			OAuthAuthorization oauth = createOAuthAuthorization();
			String callbackURL = netabareBlockConfig.getDomain() +"/accessToken"; //$NON-NLS-1$
			session.setAttribute("requestToken", oauth.getOAuthRequestToken(callbackURL)); //$NON-NLS-1$
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken");
			mav.setViewName("redirect:" + requestToken.getAuthenticationURL()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (TwitterException e) {
			// TODO 自動生成された catch ブロック
			mav.setViewName("/"); //$NON-NLS-1$
		}
		return mav;
	}

	@RequestMapping(value ="accessToken")
	public ModelAndView accessToken(ModelAndView mav) {

		try {
			RequestToken requestToken = (RequestToken) session.getAttribute("requestToken"); //$NON-NLS-1$

			AccessToken accessToken = new AccessToken(requestToken.getToken(), requestToken.getTokenSecret());

			OAuthAuthorization oath = createOAuthAuthorization();
			oath.setOAuthAccessToken(accessToken);
			String verifier = request.getParameter("oauth_verifier"); //$NON-NLS-1$
			accessToken = oath.getOAuthAccessToken(verifier);
			Twitter twitter = new TwitterFactory(createConfiguration()).getInstance(accessToken);

			UserAccountData userAccountData = userAccountDataRepository.findByUserid(twitter.getId());
			if(userAccountData == null)
			{
				userAccountData = new UserAccountData();
				userAccountData.setUserid(twitter.getId());
			}
			userAccountData.setAccessToken(accessToken.getToken());
			userAccountData.setAccessTokenSecret(accessToken.getTokenSecret());
			userAccountDataRepository.saveAndFlush(userAccountData);

			List<NetabareAccountData> list = netabareAccountDataRepository.findAll();
			String id = (String) session.getAttribute("id");

			if(id != null)
			{
				list.clear();
				NetabareAccountData netabareAccountData = new NetabareAccountData();
				netabareAccountData.setUserid(new Long(id));
				list.add(netabareAccountData);
				if(userAccountData.getUserid().equals(netabareBlockConfig.getAdminTwitterId()))
				{
					netabareAccountDataRepository.saveAndFlush(netabareAccountData);
				}

			}

			for (NetabareAccountData netabareAccountData : list) {
				try{
					Long netabareid = netabareAccountData.getUserid();
					twitter.reportSpam(netabareid);
					twitter.createBlock(netabareid);
					Relationship rs = twitter.showFriendship(twitter.getId(), netabareid);
					if(rs.isSourceBlockingTarget())
					{
						netabareAccountData.setResult(":ブロックしました");
					}else
					{
						netabareAccountData.setResult(":ブロックに失敗しました");
					}
				} catch (TwitterException  |  NumberFormatException e) {
					netabareAccountData.setResult(":" + e.getMessage());
				}
			}
			session.setAttribute("ret", "ブロック実施結果");
			session.setAttribute("listNetabareAccount", list);
			session.setAttribute("niconico",true);

		} catch (TwitterException  |  NumberFormatException e) {
			// TODO 自動生成された catch ブロック
			session.setAttribute("ret", e.getMessage());
		}
		session.removeAttribute("id");
		mav.setViewName("redirect:/#result");

		return mav;
	}

	@RequestMapping(value = "/idset",method = RequestMethod.POST )
	public ModelAndView ConfigPost(
			@ModelAttribute("id") String id,
			BindingResult bindingResult,
			ModelAndView mav) {
		mav.setViewName("redirect:requestToken"); //$NON-NLS-1$
		session.setAttribute("id", id);
		return mav;
	}
	private OAuthAuthorization createOAuthAuthorization() {
		return new OAuthAuthorization(createConfiguration());
	}

	private Configuration createConfiguration() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(netabareBlockConfig.getConsumerKey()); //$NON-NLS-1$
		builder.setOAuthConsumerSecret(netabareBlockConfig.getConsumerSecret()); //$NON-NLS-1$
		builder.setOAuthAccessToken(null);
		builder.setOAuthAccessTokenSecret(null);
		return (builder.build());
	}

}
