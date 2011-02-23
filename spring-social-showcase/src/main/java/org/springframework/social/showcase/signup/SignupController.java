/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.showcase.signup;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.social.showcase.ShowcaseUser;
import org.springframework.social.showcase.UserRepository;
import org.springframework.social.showcase.UsernameAlreadyInUseException;
import org.springframework.social.web.connect.SignInControllerService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class SignupController {

	private final UserRepository userRepository;
	private final SignInControllerService signinService;

	@Inject
	public SignupController(UserRepository userRepository, SignInControllerService signinService) {
		this.userRepository = userRepository;
		this.signinService = signinService;
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public SignupForm signupForm(@RequestParam(required = false) String deferredConnectionUrl, WebRequest request) {
		if(deferredConnectionUrl != null) {
			request.setAttribute("redirectAfterSignupUrl", deferredConnectionUrl, WebRequest.SCOPE_SESSION);
		}
		return new SignupForm();
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String signup(@Valid SignupForm form, BindingResult formBinding, WebRequest request) {
		if (formBinding.hasErrors()) {
			return null;
		}

		if (createUser(form, formBinding)) {
			String deferredConnectUri = (String) request.getAttribute("redirectAfterSignupUrl", WebRequest.SCOPE_SESSION);
			return deferredConnectUri != null ? "redirect:" + deferredConnectUri : "redirect:/";
		}

		return null;
	}

	private boolean createUser(SignupForm form, BindingResult formBinding) {
		try {
			ShowcaseUser user = new ShowcaseUser(form.getUsername(), form.getPassword(),
					form.getFirstName(), form.getLastName());
			userRepository.createUser(user);

			signinService.signIn(user.getUsername());
			return true;
		} catch (UsernameAlreadyInUseException e) {
			formBinding.rejectValue("username", "user.duplicateUsername", "already in use");
			return false;
		}
	}
}
