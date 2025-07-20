package com.BE.QuitCare.service.oauth2;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.AuthProvider;
import com.BE.QuitCare.exception.OAuth2AuthenticationProcessingException;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.service.oauth2.user.OAuth2UserInfo;
import com.BE.QuitCare.service.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthenticationRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<Account> accountOptional = accountRepository.findByEmail(oAuth2UserInfo.getEmail());
        Account account;

        AuthProvider provider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId());

        if (accountOptional.isPresent()) {
            account = accountOptional.get();

            if (!account.getProvider().equals(provider)) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        account.getProvider() + " account. Please use your " + account.getProvider() + " account to login.");
            }

            account = updateExistingAccount(account, oAuth2UserInfo);

        } else {
            account = registerNewAccount(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(account, oAuth2User.getAttributes());
    }

    private Account registerNewAccount(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Account account = new Account();
        account.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        account.setProviderId(oAuth2UserInfo.getId());
        account.setEmail(oAuth2UserInfo.getEmail());
        account.setFullName(oAuth2UserInfo.getName());
        account.setAvatar(oAuth2UserInfo.getImageUrl());
        account.setStatus(AccountStatus.ACTIVE); // default status

        return accountRepository.save(account);
    }

    private Account updateExistingAccount(Account account, OAuth2UserInfo oAuth2UserInfo) {
        account.setFullName(oAuth2UserInfo.getName());
        account.setAvatar(oAuth2UserInfo.getImageUrl());
        return accountRepository.save(account);
    }
}
