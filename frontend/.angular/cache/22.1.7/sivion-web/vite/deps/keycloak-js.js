import { n as _objectSpread2, r as _defineProperty, t as _asyncToGenerator } from "./asyncToGenerator-BJgYDK9H.js";
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/checkPrivateRedeclaration.js
function _checkPrivateRedeclaration(e, t) {
	if (t.has(e)) throw new TypeError("Cannot initialize the same private elements twice on an object");
}
//#endregion
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/classPrivateMethodInitSpec.js
function _classPrivateMethodInitSpec(e, a) {
	_checkPrivateRedeclaration(e, a), a.add(e);
}
//#endregion
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/classPrivateFieldInitSpec.js
function _classPrivateFieldInitSpec(e, t, a) {
	_checkPrivateRedeclaration(e, t), t.set(e, a);
}
//#endregion
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/assertClassBrand.js
function _assertClassBrand(e, t, n) {
	if ("function" == typeof e ? e === t : e.has(t)) return arguments.length < 3 ? t : n;
	throw new TypeError("Private element is not present on this object");
}
//#endregion
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/classPrivateFieldSet2.js
function _classPrivateFieldSet2(s, a, r) {
	return s.set(_assertClassBrand(s, a), r), r;
}
//#endregion
//#region \0@oxc-project+runtime@0.139.0/helpers/esm/classPrivateFieldGet2.js
function _classPrivateFieldGet2(s, a) {
	return s.get(_assertClassBrand(s, a));
}
//#endregion
//#region node_modules/keycloak-js/lib/keycloak.js
/**
* @import {Acr, KeycloakAccountOptions, KeycloakAdapter, KeycloakConfig, KeycloakError, KeycloakFlow, KeycloakInitOptions, KeycloakLoginOptions, KeycloakLogoutOptions, KeycloakPkceMethod, KeycloakProfile, KeycloakRegisterOptions, KeycloakResourceAccess, KeycloakResponseMode, KeycloakResponseType, KeycloakRoles, KeycloakTokenParsed, OpenIdProviderMetadata} from "./keycloak.ts"
*/
var CONTENT_TYPE_JSON = "application/json";
var _refreshQueue = /* @__PURE__ */ new WeakMap();
var _adapter = /* @__PURE__ */ new WeakMap();
var _useNonce = /* @__PURE__ */ new WeakMap();
var _callbackStorage = /* @__PURE__ */ new WeakMap();
var _logInfo = /* @__PURE__ */ new WeakMap();
var _logWarn = /* @__PURE__ */ new WeakMap();
var _loginIframe = /* @__PURE__ */ new WeakMap();
var _config = /* @__PURE__ */ new WeakMap();
var _Keycloak_brand = /* @__PURE__ */ new WeakSet();
/**
* @typedef {Object} Endpoints
* @property {() => string} authorize
* @property {() => string} token
* @property {() => string} logout
* @property {() => string} checkSessionIframe
* @property {() => string=} thirdPartyCookiesIframe
* @property {() => string} register
* @property {() => string} userinfo
*/
/**
* @typedef {Object} LoginIframe
* @property {boolean} enable
* @property {((error: Error | null, value?: boolean) => void)[]} callbackList
* @property {number} interval
* @property {HTMLIFrameElement=} iframe
* @property {string=} iframeOrigin
*/
var Keycloak = class {
	/**
	* @param {KeycloakConfig} config
	*/
	constructor(config) {
		var _this = this;
		_classPrivateMethodInitSpec(this, _Keycloak_brand);
		_classPrivateFieldInitSpec(this, _refreshQueue, []);
		_classPrivateFieldInitSpec(this, _adapter, void 0);
		_classPrivateFieldInitSpec(this, _useNonce, true);
		_classPrivateFieldInitSpec(this, _callbackStorage, void 0);
		_classPrivateFieldInitSpec(this, _logInfo, _assertClassBrand(_Keycloak_brand, this, _createLogger).call(this, console.info));
		_classPrivateFieldInitSpec(this, _logWarn, _assertClassBrand(_Keycloak_brand, this, _createLogger).call(this, console.warn));
		_classPrivateFieldInitSpec(this, _loginIframe, {
			enable: true,
			callbackList: [],
			interval: 5
		});
		_classPrivateFieldInitSpec(this, _config, void 0);
		_defineProperty(this, "didInitialize", false);
		_defineProperty(this, "authenticated", false);
		_defineProperty(this, "loginRequired", false);
		_defineProperty(
			this,
			/** @type {KeycloakResponseMode} */
			"responseMode",
			"fragment"
		);
		_defineProperty(
			this,
			/** @type {KeycloakResponseType} */
			"responseType",
			"code"
		);
		_defineProperty(
			this,
			/** @type {KeycloakFlow} */
			"flow",
			"standard"
		);
		_defineProperty(
			this,
			/** @type {number?} */
			"timeSkew",
			null
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"redirectUri",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"silentCheckSsoRedirectUri",
			void 0
		);
		_defineProperty(
			this,
			/** @type {boolean} */
			"silentCheckSsoFallback",
			true
		);
		_defineProperty(
			this,
			/** @type {KeycloakPkceMethod} */
			"pkceMethod",
			"S256"
		);
		_defineProperty(this, "enableLogging", false);
		_defineProperty(
			this,
			/** @type {'GET' | 'POST'} */
			"logoutMethod",
			"GET"
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"scope",
			void 0
		);
		_defineProperty(this, "messageReceiveTimeout", 1e4);
		_defineProperty(
			this,
			/** @type {string=} */
			"idToken",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakTokenParsed=} */
			"idTokenParsed",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"token",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakTokenParsed=} */
			"tokenParsed",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"refreshToken",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakTokenParsed=} */
			"refreshTokenParsed",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"clientId",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"sessionId",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"subject",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"authServerUrl",
			void 0
		);
		_defineProperty(
			this,
			/** @type {string=} */
			"realm",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakRoles=} */
			"realmAccess",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakResourceAccess=} */
			"resourceAccess",
			void 0
		);
		_defineProperty(
			this,
			/** @type {KeycloakProfile=} */
			"profile",
			void 0
		);
		_defineProperty(
			this,
			/** @type {{}=} */
			"userInfo",
			void 0
		);
		_defineProperty(
			this,
			/** @type {Endpoints} */
			"endpoints",
			void 0
		);
		_defineProperty(
			this,
			/** @type {number=} */
			"tokenTimeoutHandle",
			void 0
		);
		_defineProperty(
			this,
			/** @type {() => void=} */
			"onAuthSuccess",
			void 0
		);
		_defineProperty(
			this,
			/** @type {(errorData?: KeycloakError) => void=} */
			"onAuthError",
			void 0
		);
		_defineProperty(
			this,
			/** @type {() => void=} */
			"onAuthRefreshSuccess",
			void 0
		);
		_defineProperty(
			this,
			/** @type {() => void=} */
			"onAuthRefreshError",
			void 0
		);
		_defineProperty(
			this,
			/** @type {() => void=} */
			"onTokenExpired",
			void 0
		);
		_defineProperty(
			this,
			/** @type {() => void=} */
			"onAuthLogout",
			void 0
		);
		_defineProperty(
			this,
			/** @type {(authenticated: boolean) => void=} */
			"onReady",
			void 0
		);
		_defineProperty(
			this,
			/** @type {(status: 'success' | 'cancelled' | 'error', action: string) => void=} */
			"onActionUpdate",
			void 0
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakInitOptions} initOptions
			* @returns {Promise<boolean>}
			*/
			"init",
			_asyncToGenerator(function* (initOptions = {}) {
				var _this$onReady;
				if (_this.didInitialize) throw new Error("A 'Keycloak' instance can only be initialized once.");
				_this.didInitialize = true;
				_classPrivateFieldSet2(_callbackStorage, _this, createCallbackStorage());
				if (typeof initOptions.adapter === "string" && [
					"default",
					"cordova",
					"cordova-native"
				].includes(initOptions.adapter)) _classPrivateFieldSet2(_adapter, _this, _assertClassBrand(_Keycloak_brand, _this, _loadAdapter).call(_this, initOptions.adapter));
				else if (typeof initOptions.adapter === "object") _classPrivateFieldSet2(_adapter, _this, initOptions.adapter);
				else if ("Cordova" in window || "cordova" in window) _classPrivateFieldSet2(_adapter, _this, _assertClassBrand(_Keycloak_brand, _this, _loadAdapter).call(_this, "cordova"));
				else _classPrivateFieldSet2(_adapter, _this, _assertClassBrand(_Keycloak_brand, _this, _loadAdapter).call(_this, "default"));
				if (typeof initOptions.useNonce !== "undefined") _classPrivateFieldSet2(_useNonce, _this, initOptions.useNonce);
				if (typeof initOptions.checkLoginIframe !== "undefined") _classPrivateFieldGet2(_loginIframe, _this).enable = initOptions.checkLoginIframe;
				if (initOptions.checkLoginIframeInterval) _classPrivateFieldGet2(_loginIframe, _this).interval = initOptions.checkLoginIframeInterval;
				if (initOptions.onLoad === "login-required") _this.loginRequired = true;
				if (initOptions.responseMode) if (initOptions.responseMode === "query" || initOptions.responseMode === "fragment") _this.responseMode = initOptions.responseMode;
				else throw new Error("Invalid value for responseMode");
				if (initOptions.flow) {
					switch (initOptions.flow) {
						case "standard":
							_this.responseType = "code";
							break;
						case "implicit":
							_this.responseType = "id_token token";
							break;
						case "hybrid":
							_this.responseType = "code id_token token";
							break;
						default: throw new Error("Invalid value for flow");
					}
					_this.flow = initOptions.flow;
				}
				if (typeof initOptions.timeSkew === "number") _this.timeSkew = initOptions.timeSkew;
				if (initOptions.redirectUri) _this.redirectUri = initOptions.redirectUri;
				if (initOptions.silentCheckSsoRedirectUri) _this.silentCheckSsoRedirectUri = initOptions.silentCheckSsoRedirectUri;
				if (typeof initOptions.silentCheckSsoFallback === "boolean") _this.silentCheckSsoFallback = initOptions.silentCheckSsoFallback;
				if (typeof initOptions.pkceMethod !== "undefined") {
					if (initOptions.pkceMethod !== "S256" && initOptions.pkceMethod !== false) throw new TypeError(`Invalid value for pkceMethod', expected 'S256' or false but got ${initOptions.pkceMethod}.`);
					_this.pkceMethod = initOptions.pkceMethod;
				}
				if (typeof initOptions.enableLogging === "boolean") _this.enableLogging = initOptions.enableLogging;
				if (initOptions.logoutMethod === "POST") _this.logoutMethod = "POST";
				if (typeof initOptions.scope === "string") _this.scope = initOptions.scope;
				if (typeof initOptions.messageReceiveTimeout === "number" && initOptions.messageReceiveTimeout > 0) _this.messageReceiveTimeout = initOptions.messageReceiveTimeout;
				yield _assertClassBrand(_Keycloak_brand, _this, _loadConfig).call(_this);
				yield _assertClassBrand(_Keycloak_brand, _this, _check3pCookiesSupported).call(_this);
				yield _assertClassBrand(_Keycloak_brand, _this, _processInit).call(_this, initOptions);
				(_this$onReady = _this.onReady) === null || _this$onReady === void 0 || _this$onReady.call(_this, _this.authenticated);
				return _this.authenticated;
			})
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakLoginOptions} [options]
			* @returns {Promise<void>}
			*/
			"login",
			(options) => {
				return _classPrivateFieldGet2(_adapter, this).login(options);
			}
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakLoginOptions} [options]
			* @returns {Promise<string>}
			*/
			"createLoginUrl",
			function() {
				var _ref = _asyncToGenerator(function* (options) {
					const state = createUUID();
					const nonce = createUUID();
					const redirectUri = _classPrivateFieldGet2(_adapter, _this).redirectUri(options);
					/** @type {CallbackState} */
					const callbackState = {
						state,
						nonce,
						redirectUri,
						loginOptions: options
					};
					if (options === null || options === void 0 ? void 0 : options.prompt) callbackState.prompt = options.prompt;
					const url = (options === null || options === void 0 ? void 0 : options.action) === "register" ? _this.endpoints.register() : _this.endpoints.authorize();
					let scope = (options === null || options === void 0 ? void 0 : options.scope) || _this.scope;
					const scopeValues = scope ? scope.split(" ") : [];
					if (!scopeValues.includes("openid")) scopeValues.unshift("openid");
					scope = scopeValues.join(" ");
					const params = new URLSearchParams([
						["client_id", _this.clientId],
						["redirect_uri", redirectUri],
						["state", state],
						["response_mode", _this.responseMode],
						["response_type", _this.responseType],
						["scope", scope]
					]);
					if (_classPrivateFieldGet2(_useNonce, _this)) params.append("nonce", nonce);
					if (options === null || options === void 0 ? void 0 : options.prompt) params.append("prompt", options.prompt);
					if (typeof (options === null || options === void 0 ? void 0 : options.maxAge) === "number") params.append("max_age", options.maxAge.toString());
					if (options === null || options === void 0 ? void 0 : options.loginHint) params.append("login_hint", options.loginHint);
					if (options === null || options === void 0 ? void 0 : options.idpHint) params.append("kc_idp_hint", options.idpHint);
					if ((options === null || options === void 0 ? void 0 : options.action) && options.action !== "register") params.append("kc_action", options.action);
					if (options === null || options === void 0 ? void 0 : options.locale) params.append("ui_locales", options.locale);
					if (options === null || options === void 0 ? void 0 : options.acr) params.append("claims", buildClaimsParameter(options.acr));
					if (options === null || options === void 0 ? void 0 : options.acrValues) params.append("acr_values", options.acrValues);
					if (_this.pkceMethod) try {
						const codeVerifier = generateCodeVerifier(96);
						const pkceChallenge = yield generatePkceChallenge(_this.pkceMethod, codeVerifier);
						callbackState.pkceCodeVerifier = codeVerifier;
						params.append("code_challenge", pkceChallenge);
						params.append("code_challenge_method", _this.pkceMethod);
					} catch (error) {
						throw new Error("Failed to generate PKCE challenge.", { cause: error });
					}
					_classPrivateFieldGet2(_callbackStorage, _this).add(callbackState);
					return `${url}?${params.toString()}`;
				});
				return function(_x) {
					return _ref.apply(this, arguments);
				};
			}()
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakLogoutOptions} [options]
			* @returns {Promise<void>}
			*/
			"logout",
			(options) => {
				return _classPrivateFieldGet2(_adapter, this).logout(options);
			}
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakLogoutOptions} [options]
			* @returns {string}
			*/
			"createLogoutUrl",
			(options) => {
				var _options$logoutMethod;
				const logoutMethod = (_options$logoutMethod = options === null || options === void 0 ? void 0 : options.logoutMethod) !== null && _options$logoutMethod !== void 0 ? _options$logoutMethod : this.logoutMethod;
				const url = this.endpoints.logout();
				if (logoutMethod === "POST") return url;
				const params = new URLSearchParams([["client_id", this.clientId], ["post_logout_redirect_uri", _classPrivateFieldGet2(_adapter, this).redirectUri(options)]]);
				if (this.idToken) params.append("id_token_hint", this.idToken);
				return `${url}?${params.toString()}`;
			}
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakRegisterOptions} [options]
			* @returns {Promise<void>}
			*/
			"register",
			(options) => {
				return _classPrivateFieldGet2(_adapter, this).register(options);
			}
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakRegisterOptions} [options]
			* @returns {Promise<string>}
			*/
			"createRegisterUrl",
			(options) => {
				return this.createLoginUrl(_objectSpread2(_objectSpread2({}, options), {}, { action: "register" }));
			}
		);
		_defineProperty(
			this,
			/**
			* @param {KeycloakAccountOptions} [options]
			* @returns {string}
			*/
			"createAccountUrl",
			(options) => {
				const url = _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this);
				if (!url) throw new Error("Unable to create account URL, make sure the adapter is not configured using a generic OIDC provider.");
				return `${url}/account?${new URLSearchParams([["referrer", this.clientId], ["referrer_uri", _classPrivateFieldGet2(_adapter, this).redirectUri(options)]]).toString()}`;
			}
		);
		_defineProperty(
			this,
			/**
			* @returns {Promise<void>}
			*/
			"accountManagement",
			() => {
				return _classPrivateFieldGet2(_adapter, this).accountManagement();
			}
		);
		_defineProperty(
			this,
			/**
			* @param {string} role
			* @returns {boolean}
			*/
			"hasRealmRole",
			(role) => {
				const access = this.realmAccess;
				return !!access && access.roles.indexOf(role) >= 0;
			}
		);
		_defineProperty(
			this,
			/**
			* @param {string} role
			* @param {string} [resource]
			* @returns {boolean}
			*/
			"hasResourceRole",
			(role, resource) => {
				if (!this.resourceAccess) return false;
				const access = this.resourceAccess[resource || this.clientId];
				return !!access && access.roles.indexOf(role) >= 0;
			}
		);
		_defineProperty(
			this,
			/**
			* @returns {Promise<KeycloakProfile>}
			*/
			"loadUserProfile",
			_asyncToGenerator(function* () {
				const realmUrl = _assertClassBrand(_Keycloak_brand, _this, _getRealmUrl).call(_this);
				if (!realmUrl) throw new Error("Unable to load user profile, make sure the adapter is not configured using a generic OIDC provider.");
				return _this.profile = yield fetchJSON(`${realmUrl}/account`, { headers: [buildAuthorizationHeader(_this.token)] });
			})
		);
		_defineProperty(
			this,
			/**
			* @returns {Promise<{}>}
			*/
			"loadUserInfo",
			_asyncToGenerator(function* () {
				return _this.userInfo = yield fetchJSON(_this.endpoints.userinfo(), { headers: [buildAuthorizationHeader(_this.token)] });
			})
		);
		_defineProperty(
			this,
			/**
			* @param {number} [minValidity]
			* @returns {boolean}
			*/
			"isTokenExpired",
			(minValidity) => {
				if (!this.tokenParsed || !this.refreshToken && this.flow !== "implicit") throw new Error("Not authenticated");
				if (this.timeSkew == null) {
					_classPrivateFieldGet2(_logInfo, this).call(this, "[KEYCLOAK] Unable to determine if token is expired as timeskew is not set");
					return true;
				}
				if (typeof this.tokenParsed.exp !== "number") return false;
				let expiresIn = this.tokenParsed.exp - Math.ceil((/* @__PURE__ */ new Date()).getTime() / 1e3) + this.timeSkew;
				if (minValidity) {
					if (isNaN(minValidity)) throw new Error("Invalid minValidity");
					expiresIn -= minValidity;
				}
				return expiresIn < 0;
			}
		);
		_defineProperty(
			this,
			/**
			* @param {number} minValidity
			* @returns {Promise<boolean>}
			*/
			"updateToken",
			function() {
				var _ref2 = _asyncToGenerator(function* (minValidity) {
					if (!_this.refreshToken) throw new Error("Unable to update token, no refresh token available.");
					minValidity = minValidity || 5;
					if (_classPrivateFieldGet2(_loginIframe, _this).enable) yield _assertClassBrand(_Keycloak_brand, _this, _checkLoginIframe).call(_this);
					let refreshToken = false;
					if (minValidity === -1) {
						refreshToken = true;
						_classPrivateFieldGet2(_logInfo, _this).call(_this, "[KEYCLOAK] Refreshing token: forced refresh");
					} else if (!_this.tokenParsed || _this.isTokenExpired(minValidity)) {
						refreshToken = true;
						_classPrivateFieldGet2(_logInfo, _this).call(_this, "[KEYCLOAK] Refreshing token: token expired");
					}
					if (!refreshToken) return false;
					/** @type {PromiseWithResolvers<boolean>} */
					const { promise, resolve, reject } = Promise.withResolvers();
					_classPrivateFieldGet2(_refreshQueue, _this).push({
						resolve,
						reject
					});
					if (_classPrivateFieldGet2(_refreshQueue, _this).length === 1) {
						const url = _this.endpoints.token();
						let timeLocal = (/* @__PURE__ */ new Date()).getTime();
						try {
							var _this$onAuthRefreshSu;
							const response = yield fetchRefreshToken(url, _this.refreshToken, _this.clientId);
							_classPrivateFieldGet2(_logInfo, _this).call(_this, "[KEYCLOAK] Token refreshed");
							timeLocal = (timeLocal + (/* @__PURE__ */ new Date()).getTime()) / 2;
							_assertClassBrand(_Keycloak_brand, _this, _setToken).call(_this, response.access_token, response.refresh_token, response.id_token, timeLocal);
							(_this$onAuthRefreshSu = _this.onAuthRefreshSuccess) === null || _this$onAuthRefreshSu === void 0 || _this$onAuthRefreshSu.call(_this);
							for (let p = _classPrivateFieldGet2(_refreshQueue, _this).pop(); p != null; p = _classPrivateFieldGet2(_refreshQueue, _this).pop()) p.resolve(true);
						} catch (error) {
							var _this$onAuthRefreshEr;
							_classPrivateFieldGet2(_logWarn, _this).call(_this, "[KEYCLOAK] Failed to refresh token");
							if (error instanceof NetworkError && error.response.status === 400) _this.clearToken();
							(_this$onAuthRefreshEr = _this.onAuthRefreshError) === null || _this$onAuthRefreshEr === void 0 || _this$onAuthRefreshEr.call(_this);
							for (let p = _classPrivateFieldGet2(_refreshQueue, _this).pop(); p != null; p = _classPrivateFieldGet2(_refreshQueue, _this).pop()) p.reject(error);
						}
					}
					return yield promise;
				});
				return function(_x2) {
					return _ref2.apply(this, arguments);
				};
			}()
		);
		_defineProperty(this, "clearToken", () => {
			if (this.token) {
				var _this$onAuthLogout;
				_assertClassBrand(_Keycloak_brand, this, _setToken).call(this);
				(_this$onAuthLogout = this.onAuthLogout) === null || _this$onAuthLogout === void 0 || _this$onAuthLogout.call(this);
				if (this.loginRequired) this.login();
			}
		});
		if (typeof config !== "string" && !isObject(config)) throw new Error("The 'Keycloak' constructor must be provided with a configuration object, or a URL to a JSON configuration file.");
		if (isObject(config)) {
			const requiredProperties = "oidcProvider" in config ? ["clientId"] : [
				"url",
				"realm",
				"clientId"
			];
			for (const property of requiredProperties) if (!(property in config)) throw new Error(`The configuration object is missing the required '${property}' property.`);
		}
		if (!globalThis.isSecureContext) _classPrivateFieldGet2(_logWarn, this).call(this, "[KEYCLOAK] Keycloak JS must be used in a 'secure context' to function properly as it relies on browser APIs that are otherwise not available.\nContinuing to run your application insecurely will lead to unexpected behavior and breakage.\n\nFor more information see: https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts");
		_classPrivateFieldSet2(_config, this, config);
	}
};
/**
* @param {"default" | "cordova" | "cordova-native"} type
* @returns {KeycloakAdapter}
*/
function _loadAdapter(type) {
	if (type === "default") return _assertClassBrand(_Keycloak_brand, this, _loadDefaultAdapter).call(this);
	if (type === "cordova") {
		_classPrivateFieldGet2(_loginIframe, this).enable = false;
		return _assertClassBrand(_Keycloak_brand, this, _loadCordovaAdapter).call(this);
	}
	if (type === "cordova-native") {
		_classPrivateFieldGet2(_loginIframe, this).enable = false;
		return _assertClassBrand(_Keycloak_brand, this, _loadCordovaNativeAdapter).call(this);
	}
	throw new Error("invalid adapter type: " + type);
}
/**
* @returns {KeycloakAdapter}
*/
function _loadDefaultAdapter() {
	var _this2 = this;
	/** @type {KeycloakAdapter['redirectUri']}{} */
	const redirectUri = (options) => {
		return (options === null || options === void 0 ? void 0 : options.redirectUri) || this.redirectUri || globalThis.location.href;
	};
	return {
		login: function() {
			var _ref3 = _asyncToGenerator(function* (options) {
				window.location.assign(yield _this2.createLoginUrl(options));
				return yield new Promise(() => {});
			});
			return function login(_x3) {
				return _ref3.apply(this, arguments);
			};
		}(),
		logout: function() {
			var _ref4 = _asyncToGenerator(function* (options) {
				var _options$logoutMethod2;
				if (((_options$logoutMethod2 = options === null || options === void 0 ? void 0 : options.logoutMethod) !== null && _options$logoutMethod2 !== void 0 ? _options$logoutMethod2 : _this2.logoutMethod) === "GET") {
					window.location.replace(_this2.createLogoutUrl(options));
					return;
				}
				const form = document.createElement("form");
				form.setAttribute("method", "POST");
				form.setAttribute("action", _this2.createLogoutUrl(options));
				form.style.display = "none";
				const data = {
					id_token_hint: _this2.idToken,
					client_id: _this2.clientId,
					post_logout_redirect_uri: redirectUri(options)
				};
				for (const [name, value] of Object.entries(data)) {
					const input = document.createElement("input");
					input.setAttribute("type", "hidden");
					input.setAttribute("name", name);
					input.setAttribute("value", value);
					form.appendChild(input);
				}
				document.body.appendChild(form);
				form.submit();
			});
			return function logout(_x4) {
				return _ref4.apply(this, arguments);
			};
		}(),
		register: function() {
			var _ref5 = _asyncToGenerator(function* (options) {
				window.location.assign(yield _this2.createRegisterUrl(options));
				return yield new Promise(() => {});
			});
			return function register(_x5) {
				return _ref5.apply(this, arguments);
			};
		}(),
		accountManagement: function() {
			var _ref6 = _asyncToGenerator(function* () {
				const accountUrl = _this2.createAccountUrl();
				if (typeof accountUrl !== "undefined") window.location.href = accountUrl;
				else throw new Error("Not supported by the OIDC server");
				return yield new Promise(() => {});
			});
			return function accountManagement() {
				return _ref6.apply(this, arguments);
			};
		}(),
		redirectUri
	};
}
/**
* @returns {KeycloakAdapter}
*/
function _loadCordovaAdapter() {
	var _this3 = this;
	/**
	* @param {string} loginUrl
	* @param {string} target
	* @param {string} options
	* @returns {WindowProxy | null}
	*/
	const cordovaOpenWindowWrapper = (loginUrl, target, options) => {
		if (window.cordova && window.cordova.InAppBrowser) return window.cordova.InAppBrowser.open(loginUrl, target, options);
		else return window.open(loginUrl, target, options);
	};
	const shallowCloneCordovaOptions = (userOptions) => {
		if (userOptions && userOptions.cordovaOptions) return Object.keys(userOptions.cordovaOptions).reduce((options, optionName) => {
			options[optionName] = userOptions.cordovaOptions[optionName];
			return options;
		}, {});
		else return {};
	};
	const formatCordovaOptions = (cordovaOptions) => {
		return Object.keys(cordovaOptions).reduce((options, optionName) => {
			options.push(optionName + "=" + cordovaOptions[optionName]);
			return options;
		}, []).join(",");
	};
	const createCordovaOptions = (userOptions) => {
		const cordovaOptions = shallowCloneCordovaOptions(userOptions);
		cordovaOptions.location = "no";
		if (userOptions && userOptions.prompt === "none") cordovaOptions.hidden = "yes";
		return formatCordovaOptions(cordovaOptions);
	};
	const getCordovaRedirectUri = () => {
		return this.redirectUri || "http://localhost";
	};
	return {
		login: function() {
			var _ref9 = _asyncToGenerator(function* (options) {
				const cordovaOptions = createCordovaOptions(options);
				const loginUrl = yield _this3.createLoginUrl(options);
				const ref = cordovaOpenWindowWrapper(loginUrl, "_blank", cordovaOptions);
				let completed = false;
				let closed = false;
				function closeBrowser() {
					closed = true;
					ref.close();
				}
				return yield new Promise((resolve, reject) => {
					ref.addEventListener("loadstart", function() {
						var _ref7 = _asyncToGenerator(function* (event) {
							if (event.url.indexOf(getCordovaRedirectUri()) === 0) {
								const callback = _assertClassBrand(_Keycloak_brand, _this3, _parseCallback).call(_this3, event.url);
								completed = true;
								closeBrowser();
								try {
									yield _assertClassBrand(_Keycloak_brand, _this3, _processCallback).call(_this3, callback);
									resolve();
								} catch (error) {
									reject(error);
								}
							}
						});
						return function(_x6) {
							return _ref7.apply(this, arguments);
						};
					}());
					ref.addEventListener("loaderror", function() {
						var _ref8 = _asyncToGenerator(function* (event) {
							if (!completed) if (event.url.indexOf(getCordovaRedirectUri()) === 0) {
								const callback = _assertClassBrand(_Keycloak_brand, _this3, _parseCallback).call(_this3, event.url);
								completed = true;
								closeBrowser();
								try {
									yield _assertClassBrand(_Keycloak_brand, _this3, _processCallback).call(_this3, callback);
									resolve();
								} catch (error) {
									reject(error);
								}
							} else {
								reject(/* @__PURE__ */ new Error("Unable to process login."));
								closeBrowser();
							}
						});
						return function(_x7) {
							return _ref8.apply(this, arguments);
						};
					}());
					ref.addEventListener("exit", function(event) {
						if (!closed) reject(/* @__PURE__ */ new Error("User closed the login window."));
					});
				});
			});
			return function login(_x8) {
				return _ref9.apply(this, arguments);
			};
		}(),
		logout: function() {
			var _ref10 = _asyncToGenerator(function* (options) {
				const logoutUrl = _this3.createLogoutUrl(options);
				const ref = cordovaOpenWindowWrapper(logoutUrl, "_blank", "location=no,hidden=yes,clearcache=yes");
				let error = false;
				ref.addEventListener("loadstart", (event) => {
					if (event.url.indexOf(getCordovaRedirectUri()) === 0) ref.close();
				});
				ref.addEventListener("loaderror", (event) => {
					if (event.url.indexOf(getCordovaRedirectUri()) === 0) ref.close();
					else {
						error = true;
						ref.close();
					}
				});
				yield new Promise((resolve, reject) => {
					ref.addEventListener("exit", () => {
						if (error) reject(/* @__PURE__ */ new Error("User closed the login window."));
						else {
							_this3.clearToken();
							resolve();
						}
					});
				});
			});
			return function logout(_x9) {
				return _ref10.apply(this, arguments);
			};
		}(),
		register: function() {
			var _ref12 = _asyncToGenerator(function* (options) {
				const registerUrl = yield _this3.createRegisterUrl();
				const cordovaOptions = createCordovaOptions(options);
				const ref = cordovaOpenWindowWrapper(registerUrl, "_blank", cordovaOptions);
				yield new Promise((resolve, reject) => {
					ref.addEventListener("loadstart", function() {
						var _ref11 = _asyncToGenerator(function* (event) {
							if (event.url.indexOf(getCordovaRedirectUri()) === 0) {
								ref.close();
								const oauth = _assertClassBrand(_Keycloak_brand, _this3, _parseCallback).call(_this3, event.url);
								try {
									yield _assertClassBrand(_Keycloak_brand, _this3, _processCallback).call(_this3, oauth);
									resolve();
								} catch (error) {
									reject(error);
								}
							}
						});
						return function(_x10) {
							return _ref11.apply(this, arguments);
						};
					}());
				});
			});
			return function register(_x11) {
				return _ref12.apply(this, arguments);
			};
		}(),
		accountManagement: function() {
			var _ref13 = _asyncToGenerator(function* () {
				const accountUrl = _this3.createAccountUrl();
				if (typeof accountUrl !== "undefined") {
					const ref = cordovaOpenWindowWrapper(accountUrl, "_blank", "location=no");
					ref.addEventListener("loadstart", function(event) {
						if (event.url.indexOf(getCordovaRedirectUri()) === 0) ref.close();
					});
				} else throw new Error("Not supported by the OIDC server");
			});
			return function accountManagement() {
				return _ref13.apply(this, arguments);
			};
		}(),
		redirectUri: () => {
			return getCordovaRedirectUri();
		}
	};
}
/**
* @returns {KeycloakAdapter}
*/
function _loadCordovaNativeAdapter() {
	var _this4 = this;
	return {
		login: function() {
			var _ref15 = _asyncToGenerator(function* (options) {
				const loginUrl = yield _this4.createLoginUrl(options);
				yield new Promise((resolve, reject) => {
					universalLinks.subscribe("keycloak", function() {
						var _ref14 = _asyncToGenerator(function* (event) {
							universalLinks.unsubscribe("keycloak");
							window.cordova.plugins.browsertab.close();
							const oauth = _assertClassBrand(_Keycloak_brand, _this4, _parseCallback).call(_this4, event.url);
							try {
								yield _assertClassBrand(_Keycloak_brand, _this4, _processCallback).call(_this4, oauth);
								resolve();
							} catch (error) {
								reject(error);
							}
						});
						return function(_x12) {
							return _ref14.apply(this, arguments);
						};
					}());
					window.cordova.plugins.browsertab.openUrl(loginUrl);
				});
			});
			return function login(_x13) {
				return _ref15.apply(this, arguments);
			};
		}(),
		logout: function() {
			var _ref16 = _asyncToGenerator(function* (options) {
				const logoutUrl = _this4.createLogoutUrl(options);
				yield new Promise((resolve) => {
					universalLinks.subscribe("keycloak", () => {
						universalLinks.unsubscribe("keycloak");
						window.cordova.plugins.browsertab.close();
						_this4.clearToken();
						resolve();
					});
					window.cordova.plugins.browsertab.openUrl(logoutUrl);
				});
			});
			return function logout(_x14) {
				return _ref16.apply(this, arguments);
			};
		}(),
		register: function() {
			var _ref18 = _asyncToGenerator(function* (options) {
				const registerUrl = yield _this4.createRegisterUrl(options);
				yield new Promise((resolve, reject) => {
					universalLinks.subscribe("keycloak", function() {
						var _ref17 = _asyncToGenerator(function* (event) {
							universalLinks.unsubscribe("keycloak");
							window.cordova.plugins.browsertab.close();
							const oauth = _assertClassBrand(_Keycloak_brand, _this4, _parseCallback).call(_this4, event.url);
							try {
								yield _assertClassBrand(_Keycloak_brand, _this4, _processCallback).call(_this4, oauth);
								resolve();
							} catch (error) {
								reject(error);
							}
						});
						return function(_x15) {
							return _ref17.apply(this, arguments);
						};
					}());
					window.cordova.plugins.browsertab.openUrl(registerUrl);
				});
			});
			return function register(_x16) {
				return _ref18.apply(this, arguments);
			};
		}(),
		accountManagement: function() {
			var _ref19 = _asyncToGenerator(function* () {
				const accountUrl = _this4.createAccountUrl();
				if (typeof accountUrl !== "undefined") window.cordova.plugins.browsertab.openUrl(accountUrl);
				else throw new Error("Not supported by the OIDC server");
			});
			return function accountManagement() {
				return _ref19.apply(this, arguments);
			};
		}(),
		redirectUri: (options) => {
			if (options && options.redirectUri) return options.redirectUri;
			else if (this.redirectUri) return this.redirectUri;
			else return "http://localhost";
		}
	};
}
/**
* @returns {Promise<void>}
*/
function _loadConfig() {
	var _this5 = this;
	return _asyncToGenerator(function* () {
		if (typeof _classPrivateFieldGet2(_config, _this5) === "string") {
			const jsonConfig = yield fetchJsonConfig(_classPrivateFieldGet2(_config, _this5));
			_this5.authServerUrl = jsonConfig["auth-server-url"];
			_this5.realm = jsonConfig.realm;
			_this5.clientId = jsonConfig.resource;
			_assertClassBrand(_Keycloak_brand, _this5, _setupEndpoints).call(_this5);
		} else {
			_this5.clientId = _classPrivateFieldGet2(_config, _this5).clientId;
			if ("oidcProvider" in _classPrivateFieldGet2(_config, _this5)) yield _assertClassBrand(_Keycloak_brand, _this5, _loadOidcConfig).call(_this5, _classPrivateFieldGet2(_config, _this5).oidcProvider);
			else {
				_this5.authServerUrl = _classPrivateFieldGet2(_config, _this5).url;
				_this5.realm = _classPrivateFieldGet2(_config, _this5).realm;
				_assertClassBrand(_Keycloak_brand, _this5, _setupEndpoints).call(_this5);
			}
		}
	})();
}
/**
* @returns {void}
*/
function _setupEndpoints() {
	this.endpoints = {
		authorize: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/auth";
		},
		token: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/token";
		},
		logout: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/logout";
		},
		checkSessionIframe: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/login-status-iframe.html";
		},
		thirdPartyCookiesIframe: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/3p-cookies/step1.html";
		},
		register: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/registrations";
		},
		userinfo: () => {
			return _assertClassBrand(_Keycloak_brand, this, _getRealmUrl).call(this) + "/protocol/openid-connect/userinfo";
		}
	};
}
/**
* @param {string | OpenIdProviderMetadata} oidcProvider
* @returns {Promise<void>}
*/
function _loadOidcConfig(oidcProvider) {
	var _this6 = this;
	return _asyncToGenerator(function* () {
		if (typeof oidcProvider === "string") {
			const openIdConfig = yield fetchOpenIdConfig(`${stripTrailingSlash(oidcProvider)}/.well-known/openid-configuration`);
			_assertClassBrand(_Keycloak_brand, _this6, _setupOidcEndpoints).call(_this6, openIdConfig);
		} else _assertClassBrand(_Keycloak_brand, _this6, _setupOidcEndpoints).call(_this6, oidcProvider);
	})();
}
/**
* @param {OpenIdProviderMetadata} config
* @returns {void}
*/
function _setupOidcEndpoints(config) {
	this.endpoints = {
		authorize() {
			return config.authorization_endpoint;
		},
		token() {
			return config.token_endpoint;
		},
		logout() {
			if (!config.end_session_endpoint) throw new Error("Not supported by the OIDC server");
			return config.end_session_endpoint;
		},
		checkSessionIframe() {
			if (!config.check_session_iframe) throw new Error("Not supported by the OIDC server");
			return config.check_session_iframe;
		},
		register() {
			throw new Error("Redirection to \"Register user\" page not supported in standard OIDC mode");
		},
		userinfo() {
			if (!config.userinfo_endpoint) throw new Error("Not supported by the OIDC server");
			return config.userinfo_endpoint;
		}
	};
}
/**
* @returns {Promise<void>}
*/
function _check3pCookiesSupported() {
	var _this7 = this;
	return _asyncToGenerator(function* () {
		if (!_classPrivateFieldGet2(_loginIframe, _this7).enable && !_this7.silentCheckSsoRedirectUri || typeof _this7.endpoints.thirdPartyCookiesIframe !== "function") return;
		const iframe = document.createElement("iframe");
		iframe.setAttribute("src", _this7.endpoints.thirdPartyCookiesIframe());
		iframe.setAttribute("sandbox", "allow-storage-access-by-user-activation allow-scripts allow-same-origin");
		iframe.setAttribute("title", "keycloak-3p-check-iframe");
		iframe.style.display = "none";
		document.body.appendChild(iframe);
		return yield applyTimeoutToPromise(new Promise((resolve) => {
			/**
			* @param {MessageEvent} event
			*/
			const messageCallback = (event) => {
				if (iframe.contentWindow !== event.source) return;
				if (event.data !== "supported" && event.data !== "unsupported") return;
				else if (event.data === "unsupported") {
					_classPrivateFieldGet2(_logWarn, _this7).call(_this7, "[KEYCLOAK] Your browser is blocking access to 3rd-party cookies, this means:\n\n - It is not possible to retrieve tokens without redirecting to the Keycloak server (a.k.a. no support for silent authentication).\n - It is not possible to automatically detect changes to the session status (such as the user logging out in another tab).\n\nFor more information see: https://www.keycloak.org/securing-apps/javascript-adapter#_modern_browsers");
					_classPrivateFieldGet2(_loginIframe, _this7).enable = false;
					if (_this7.silentCheckSsoFallback) _this7.silentCheckSsoRedirectUri = void 0;
				}
				document.body.removeChild(iframe);
				window.removeEventListener("message", messageCallback);
				resolve();
			};
			window.addEventListener("message", messageCallback, false);
		}), _this7.messageReceiveTimeout, "Timeout when waiting for 3rd party check iframe message.");
	})();
}
/**
* @param {KeycloakInitOptions} initOptions
* @returns {Promise<void>}
*/
function _processInit(initOptions) {
	var _this8 = this;
	return _asyncToGenerator(function* () {
		const callback = _assertClassBrand(_Keycloak_brand, _this8, _parseCallback).call(_this8, window.location.href);
		if (callback === null || callback === void 0 ? void 0 : callback.newUrl) window.history.replaceState(window.history.state, "", callback.newUrl);
		if (callback && callback.valid) {
			yield _assertClassBrand(_Keycloak_brand, _this8, _setupCheckLoginIframe).call(_this8);
			yield _assertClassBrand(_Keycloak_brand, _this8, _processCallback).call(_this8, callback);
			return;
		}
		/** @param {boolean} prompt */
		const doLogin = function() {
			var _ref20 = _asyncToGenerator(function* (prompt) {
				/** @type {KeycloakLoginOptions} */
				const options = {};
				if (!prompt) options.prompt = "none";
				if (initOptions.locale) options.locale = initOptions.locale;
				yield _this8.login(options);
			});
			return function doLogin(_x17) {
				return _ref20.apply(this, arguments);
			};
		}();
		const onLoad = function() {
			var _ref21 = _asyncToGenerator(function* () {
				switch (initOptions.onLoad) {
					case "check-sso":
						if (_classPrivateFieldGet2(_loginIframe, _this8).enable) {
							yield _assertClassBrand(_Keycloak_brand, _this8, _setupCheckLoginIframe).call(_this8);
							if (!(yield _assertClassBrand(_Keycloak_brand, _this8, _checkLoginIframe).call(_this8))) _this8.silentCheckSsoRedirectUri ? yield _assertClassBrand(_Keycloak_brand, _this8, _checkSsoSilently).call(_this8) : yield doLogin(false);
						} else _this8.silentCheckSsoRedirectUri ? yield _assertClassBrand(_Keycloak_brand, _this8, _checkSsoSilently).call(_this8) : yield doLogin(false);
						break;
					case "login-required":
						yield doLogin(true);
						break;
					default: throw new Error("Invalid value for onLoad");
				}
			});
			return function onLoad() {
				return _ref21.apply(this, arguments);
			};
		}();
		if (initOptions.token && initOptions.refreshToken) {
			_assertClassBrand(_Keycloak_brand, _this8, _setToken).call(_this8, initOptions.token, initOptions.refreshToken, initOptions.idToken);
			if (_classPrivateFieldGet2(_loginIframe, _this8).enable) {
				yield _assertClassBrand(_Keycloak_brand, _this8, _setupCheckLoginIframe).call(_this8);
				if (yield _assertClassBrand(_Keycloak_brand, _this8, _checkLoginIframe).call(_this8)) {
					var _this$onAuthSuccess;
					(_this$onAuthSuccess = _this8.onAuthSuccess) === null || _this$onAuthSuccess === void 0 || _this$onAuthSuccess.call(_this8);
					_assertClassBrand(_Keycloak_brand, _this8, _scheduleCheckIframe).call(_this8);
				}
			} else try {
				var _this$onAuthSuccess2;
				yield _this8.updateToken(-1);
				(_this$onAuthSuccess2 = _this8.onAuthSuccess) === null || _this$onAuthSuccess2 === void 0 || _this$onAuthSuccess2.call(_this8);
			} catch (error) {
				var _this$onAuthError;
				(_this$onAuthError = _this8.onAuthError) === null || _this$onAuthError === void 0 || _this$onAuthError.call(_this8);
				if (initOptions.onLoad) yield onLoad();
				else throw error;
			}
		} else if (initOptions.onLoad) yield onLoad();
	})();
}
/**
* @returns {Promise<void>}
*/
function _setupCheckLoginIframe() {
	var _this9 = this;
	return _asyncToGenerator(function* () {
		if (!_classPrivateFieldGet2(_loginIframe, _this9).enable || _classPrivateFieldGet2(_loginIframe, _this9).iframe) return;
		const iframe = document.createElement("iframe");
		_classPrivateFieldGet2(_loginIframe, _this9).iframe = iframe;
		iframe.setAttribute("src", _this9.endpoints.checkSessionIframe());
		iframe.setAttribute("sandbox", "allow-storage-access-by-user-activation allow-scripts allow-same-origin");
		iframe.setAttribute("title", "keycloak-session-iframe");
		iframe.style.display = "none";
		document.body.appendChild(iframe);
		/**
		* @param {MessageEvent} event
		*/
		const messageCallback = (event) => {
			var _classPrivateFieldGet2$1;
			if (event.origin !== _classPrivateFieldGet2(_loginIframe, _this9).iframeOrigin || ((_classPrivateFieldGet2$1 = _classPrivateFieldGet2(_loginIframe, _this9).iframe) === null || _classPrivateFieldGet2$1 === void 0 ? void 0 : _classPrivateFieldGet2$1.contentWindow) !== event.source) return;
			if (!(event.data === "unchanged" || event.data === "changed" || event.data === "error")) return;
			if (event.data !== "unchanged") _this9.clearToken();
			const callbacks = _classPrivateFieldGet2(_loginIframe, _this9).callbackList;
			_classPrivateFieldGet2(_loginIframe, _this9).callbackList = [];
			for (const callback of callbacks.reverse()) if (event.data === "error") callback(/* @__PURE__ */ new Error("Error while checking login iframe"));
			else callback(null, event.data === "unchanged");
		};
		window.addEventListener("message", messageCallback, false);
		yield new Promise((resolve) => {
			iframe.addEventListener("load", () => {
				const authUrl = _this9.endpoints.authorize();
				if (authUrl.startsWith("/")) _classPrivateFieldGet2(_loginIframe, _this9).iframeOrigin = globalThis.location.origin;
				else _classPrivateFieldGet2(_loginIframe, _this9).iframeOrigin = new URL(authUrl).origin;
				resolve();
			});
		});
	})();
}
/**
* @returns {Promise<boolean | undefined>}
*/
function _checkLoginIframe() {
	var _this10 = this;
	return _asyncToGenerator(function* () {
		if (!_classPrivateFieldGet2(_loginIframe, _this10).iframe || !_classPrivateFieldGet2(_loginIframe, _this10).iframeOrigin) return;
		const message = `${_this10.clientId} ${_this10.sessionId ? _this10.sessionId : ""}`;
		const origin = _classPrivateFieldGet2(_loginIframe, _this10).iframeOrigin;
		return yield new Promise((resolve, reject) => {
			/** @type {(error: Error | null, value?: boolean) => void} */
			const callback = (error, result) => error ? reject(error) : resolve(result);
			_classPrivateFieldGet2(_loginIframe, _this10).callbackList.push(callback);
			if (_classPrivateFieldGet2(_loginIframe, _this10).callbackList.length === 1) {
				var _classPrivateFieldGet3;
				(_classPrivateFieldGet3 = _classPrivateFieldGet2(_loginIframe, _this10).iframe) === null || _classPrivateFieldGet3 === void 0 || (_classPrivateFieldGet3 = _classPrivateFieldGet3.contentWindow) === null || _classPrivateFieldGet3 === void 0 || _classPrivateFieldGet3.postMessage(message, origin);
			}
		});
	})();
}
/**
* @returns {Promise<void>}
*/
function _checkSsoSilently() {
	var _this11 = this;
	return _asyncToGenerator(function* () {
		const iframe = document.createElement("iframe");
		const src = yield _this11.createLoginUrl({
			prompt: "none",
			redirectUri: _this11.silentCheckSsoRedirectUri
		});
		iframe.setAttribute("src", src);
		iframe.setAttribute("sandbox", "allow-storage-access-by-user-activation allow-scripts allow-same-origin");
		iframe.setAttribute("title", "keycloak-silent-check-sso");
		iframe.style.display = "none";
		document.body.appendChild(iframe);
		return yield new Promise((resolve, reject) => {
			/**
			* @param {MessageEvent} event
			*/
			const messageCallback = function() {
				var _ref22 = _asyncToGenerator(function* (event) {
					if (event.origin !== window.location.origin || iframe.contentWindow !== event.source) return;
					const oauth = _assertClassBrand(_Keycloak_brand, _this11, _parseCallback).call(_this11, event.data);
					try {
						yield _assertClassBrand(_Keycloak_brand, _this11, _processCallback).call(_this11, oauth);
						resolve();
					} catch (error) {
						reject(error);
					}
					document.body.removeChild(iframe);
					window.removeEventListener("message", messageCallback);
				});
				return function messageCallback(_x18) {
					return _ref22.apply(this, arguments);
				};
			}();
			window.addEventListener("message", messageCallback);
		});
	})();
}
/**
* @param {string} url
*/
function _parseCallback(url) {
	const oauth = _assertClassBrand(_Keycloak_brand, this, _parseCallbackUrl).call(this, url);
	if (!oauth) return;
	const oauthState = _classPrivateFieldGet2(_callbackStorage, this).get(oauth.state);
	if (oauthState) {
		oauth.valid = true;
		oauth.redirectUri = oauthState.redirectUri;
		oauth.storedNonce = oauthState.nonce;
		oauth.prompt = oauthState.prompt;
		oauth.pkceCodeVerifier = oauthState.pkceCodeVerifier;
		oauth.loginOptions = oauthState.loginOptions;
	}
	return oauth;
}
/**
* @param {string} urlString
*/
function _parseCallbackUrl(urlString) {
	let supportedParams = [];
	switch (this.flow) {
		case "standard":
			supportedParams = [
				"code",
				"state",
				"session_state",
				"kc_action_status",
				"kc_action",
				"iss"
			];
			break;
		case "implicit":
			supportedParams = [
				"access_token",
				"token_type",
				"id_token",
				"state",
				"session_state",
				"expires_in",
				"kc_action_status",
				"kc_action",
				"iss"
			];
			break;
		case "hybrid":
			supportedParams = [
				"access_token",
				"token_type",
				"id_token",
				"code",
				"state",
				"session_state",
				"expires_in",
				"kc_action_status",
				"kc_action",
				"iss"
			];
			break;
	}
	supportedParams.push("error");
	supportedParams.push("error_description");
	supportedParams.push("error_uri");
	const url = new URL(urlString);
	let newUrl = "";
	let parsed;
	if (this.responseMode === "query" && url.searchParams.size > 0) {
		parsed = _assertClassBrand(_Keycloak_brand, this, _parseCallbackParams).call(this, url.search, supportedParams);
		url.search = parsed.paramsString;
		newUrl = url.toString();
	} else if (this.responseMode === "fragment" && url.hash.length > 0) {
		parsed = _assertClassBrand(_Keycloak_brand, this, _parseCallbackParams).call(this, url.hash.substring(1), supportedParams);
		url.hash = parsed.paramsString;
		newUrl = url.toString();
	}
	if (parsed === null || parsed === void 0 ? void 0 : parsed.oauthParams) {
		if (this.flow === "standard" || this.flow === "hybrid") {
			if ((parsed.oauthParams.code || parsed.oauthParams.error) && parsed.oauthParams.state) {
				parsed.oauthParams.newUrl = newUrl;
				return parsed.oauthParams;
			}
		} else if (this.flow === "implicit") {
			if ((parsed.oauthParams.access_token || parsed.oauthParams.error) && parsed.oauthParams.state) {
				parsed.oauthParams.newUrl = newUrl;
				return parsed.oauthParams;
			}
		}
	}
}
/**
* @typedef {Object} ParsedCallbackParams
* @property {string} paramsString
* @property {Record<string, string | undefined>} oauthParams
*/
/**
* @param {string} paramsString
* @param {string[]} supportedParams
* @returns {ParsedCallbackParams}
*/
function _parseCallbackParams(paramsString, supportedParams) {
	const params = paramsString.split("&");
	/** @type {Record<string, string>} */
	const oauthParams = {};
	let result = "";
	for (const param of params.reverse()) {
		const entry = new URLSearchParams(param).entries().next().value;
		if (!entry) {
			result = "&" + result;
			continue;
		}
		const [key, value] = entry;
		if (supportedParams.includes(key) && !(key in oauthParams)) oauthParams[key] = value;
		else result = result.length === 0 ? param : param + "&" + result;
	}
	return {
		paramsString: result,
		oauthParams
	};
}
function _processCallback(oauth) {
	var _this12 = this;
	return _asyncToGenerator(function* () {
		const { code, error, prompt } = oauth;
		let timeLocal = (/* @__PURE__ */ new Date()).getTime();
		/**
		* @param {string} accessToken
		* @param {string=} refreshToken
		* @param {string=} idToken
		*/
		const authSuccess = (accessToken, refreshToken, idToken) => {
			timeLocal = (timeLocal + (/* @__PURE__ */ new Date()).getTime()) / 2;
			_assertClassBrand(_Keycloak_brand, _this12, _setToken).call(_this12, accessToken, refreshToken, idToken, timeLocal);
			if (_classPrivateFieldGet2(_useNonce, _this12) && _this12.idTokenParsed && _this12.idTokenParsed.nonce !== oauth.storedNonce) {
				_classPrivateFieldGet2(_logInfo, _this12).call(_this12, "[KEYCLOAK] Invalid nonce, clearing token");
				_this12.clearToken();
				throw new Error("Invalid nonce.");
			}
		};
		if (oauth.kc_action_status) _this12.onActionUpdate && _this12.onActionUpdate(oauth.kc_action_status, oauth.kc_action);
		if (error) {
			if (prompt !== "none") if (oauth.error_description && oauth.error_description === "authentication_expired") yield _this12.login(oauth.loginOptions);
			else {
				var _this$onAuthError2;
				const errorData = {
					error,
					error_description: oauth.error_description
				};
				(_this$onAuthError2 = _this12.onAuthError) === null || _this$onAuthError2 === void 0 || _this$onAuthError2.call(_this12, errorData);
				throw errorData;
			}
			return;
		} else if (_this12.flow !== "standard" && (oauth.access_token || oauth.id_token)) {
			var _this$onAuthSuccess3;
			authSuccess(oauth.access_token, void 0, oauth.id_token);
			(_this$onAuthSuccess3 = _this12.onAuthSuccess) === null || _this$onAuthSuccess3 === void 0 || _this$onAuthSuccess3.call(_this12);
		}
		if (_this12.flow !== "implicit" && code) try {
			const response = yield fetchAccessToken(_this12.endpoints.token(), code, _this12.clientId, oauth.redirectUri, oauth.pkceCodeVerifier);
			authSuccess(response.access_token, response.refresh_token, response.id_token);
			if (_this12.flow === "standard") {
				var _this$onAuthSuccess4;
				(_this$onAuthSuccess4 = _this12.onAuthSuccess) === null || _this$onAuthSuccess4 === void 0 || _this$onAuthSuccess4.call(_this12);
			}
			_assertClassBrand(_Keycloak_brand, _this12, _scheduleCheckIframe).call(_this12);
		} catch (error) {
			var _this$onAuthError3;
			(_this$onAuthError3 = _this12.onAuthError) === null || _this$onAuthError3 === void 0 || _this$onAuthError3.call(_this12);
			throw error;
		}
	})();
}
function _scheduleCheckIframe() {
	var _this13 = this;
	return _asyncToGenerator(function* () {
		if (_classPrivateFieldGet2(_loginIframe, _this13).enable && _this13.token) {
			yield waitForTimeout(_classPrivateFieldGet2(_loginIframe, _this13).interval * 1e3);
			if (yield _assertClassBrand(_Keycloak_brand, _this13, _checkLoginIframe).call(_this13)) yield _assertClassBrand(_Keycloak_brand, _this13, _scheduleCheckIframe).call(_this13);
		}
	})();
}
/**
* @param {string} [token]
* @param {string} [refreshToken]
* @param {string} [idToken]
* @param {number} [timeLocal]
*/
function _setToken(token, refreshToken, idToken, timeLocal) {
	if (this.tokenTimeoutHandle) {
		clearTimeout(this.tokenTimeoutHandle);
		this.tokenTimeoutHandle = void 0;
	}
	if (refreshToken) {
		this.refreshToken = refreshToken;
		this.refreshTokenParsed = decodeToken(refreshToken);
	} else {
		delete this.refreshToken;
		delete this.refreshTokenParsed;
	}
	if (idToken) {
		this.idToken = idToken;
		this.idTokenParsed = decodeToken(idToken);
	} else {
		delete this.idToken;
		delete this.idTokenParsed;
	}
	if (token) {
		this.token = token;
		this.tokenParsed = decodeToken(token);
		this.sessionId = this.tokenParsed.sid;
		this.authenticated = true;
		this.subject = this.tokenParsed.sub;
		this.realmAccess = this.tokenParsed.realm_access;
		this.resourceAccess = this.tokenParsed.resource_access;
		if (timeLocal) this.timeSkew = Math.floor(timeLocal / 1e3) - this.tokenParsed.iat;
		if (this.timeSkew !== null) {
			_classPrivateFieldGet2(_logInfo, this).call(this, "[KEYCLOAK] Estimated time difference between browser and server is " + this.timeSkew + " seconds");
			if (this.onTokenExpired) {
				const expiresIn = (this.tokenParsed.exp - (/* @__PURE__ */ new Date()).getTime() / 1e3 + this.timeSkew) * 1e3;
				_classPrivateFieldGet2(_logInfo, this).call(this, "[KEYCLOAK] Token expires in " + Math.round(expiresIn / 1e3) + " s");
				if (expiresIn <= 0) this.onTokenExpired();
				else this.tokenTimeoutHandle = window.setTimeout(this.onTokenExpired, expiresIn);
			}
		}
	} else {
		delete this.token;
		delete this.tokenParsed;
		delete this.subject;
		delete this.realmAccess;
		delete this.resourceAccess;
		this.authenticated = false;
	}
}
/**
* @returns {string=}
*/
function _getRealmUrl() {
	if (typeof this.authServerUrl === "undefined") return;
	return `${stripTrailingSlash(this.authServerUrl)}/realms/${encodeURIComponent(this.realm)}`;
}
/**
* @param {Function} fn
* @returns {(message: string) => void}
*/
function _createLogger(fn) {
	return (message) => {
		if (this.enableLogging) fn.call(console, message);
	};
}
/**
* @returns {string}
*/
function createUUID() {
	if (typeof crypto === "undefined" || typeof crypto.randomUUID === "undefined") throw new Error("Web Crypto API is not available.");
	return crypto.randomUUID();
}
/**
* @param {Acr} requestedAcr
* @returns {string}
*/
function buildClaimsParameter(requestedAcr) {
	return JSON.stringify({ id_token: { acr: requestedAcr } });
}
/**
* @param {number} len
* @returns {string}
*/
function generateCodeVerifier(len) {
	return generateRandomString(len, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
}
/**
* @param {string} pkceMethod
* @param {string} codeVerifier
* @returns {Promise<string>}
*/
function generatePkceChallenge(_x19, _x20) {
	return _generatePkceChallenge.apply(this, arguments);
}
function _generatePkceChallenge() {
	_generatePkceChallenge = _asyncToGenerator(function* (pkceMethod, codeVerifier) {
		if (pkceMethod !== "S256") throw new TypeError(`Invalid value for 'pkceMethod', expected 'S256' but got '${pkceMethod}'.`);
		return bytesToBase64(new Uint8Array(yield sha256Digest(codeVerifier))).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
	});
	return _generatePkceChallenge.apply(this, arguments);
}
/**
* @param {number} len
* @param {string} alphabet
* @returns {string}
*/
function generateRandomString(len, alphabet) {
	const randomData = generateRandomData(len);
	const chars = new Array(len);
	for (let i = 0; i < len; i++) chars[i] = alphabet.charCodeAt(randomData[i] % alphabet.length);
	return String.fromCharCode.apply(null, chars);
}
/**
* @param {number} len
* @returns {Uint8Array<ArrayBuffer>}
*/
function generateRandomData(len) {
	if (typeof crypto === "undefined" || typeof crypto.getRandomValues === "undefined") throw new Error("Web Crypto API is not available.");
	return crypto.getRandomValues(new Uint8Array(len));
}
/**
* Function to extend existing native Promise with timeout
*
* @template T
* @param {Promise<T>} promise
* @param {number} timeout
* @param {string} errorMessage
* @returns {Promise<T>}
*/
function applyTimeoutToPromise(promise, timeout, errorMessage) {
	/** @type {number} */
	let timeoutHandle;
	const timeoutPromise = new Promise(function(resolve, reject) {
		timeoutHandle = window.setTimeout(function() {
			reject(new Error(errorMessage || "Promise is not settled within timeout of " + timeout + "ms"));
		}, timeout);
	});
	return Promise.race([promise, timeoutPromise]).finally(function() {
		clearTimeout(timeoutHandle);
	});
}
/**
* @returns {CallbackStorage}
*/
function createCallbackStorage() {
	try {
		return new LocalStorage();
	} catch (err) {
		return new CookieStorage();
	}
}
var STORAGE_KEY_PREFIX = "kc-callback-";
var _LocalStorage_brand = /* @__PURE__ */ new WeakSet();
/**
* @typedef {Object} CallbackState
* @property {string} state
* @property {string} nonce
* @property {string} redirectUri
* @property {KeycloakLoginOptions} [loginOptions]
* @property {KeycloakLoginOptions['prompt']} [prompt]
* @property {string} [pkceCodeVerifier]
*/
/**
* @typedef {Object} CallbackStorage
* @property {(state?: string) => CallbackState | null} get
* @property {(state: CallbackState) => void} add
*/
/**
* @implements {CallbackStorage}
*/
var LocalStorage = class {
	constructor() {
		_classPrivateMethodInitSpec(this, _LocalStorage_brand);
		globalThis.localStorage.setItem("kc-test", "test");
		globalThis.localStorage.removeItem("kc-test");
	}
	/**
	* @param {string} [state]
	* @returns {CallbackState | null}
	*/
	get(state) {
		if (!state) return null;
		_assertClassBrand(_LocalStorage_brand, this, _clearInvalidValues).call(this);
		const key = STORAGE_KEY_PREFIX + state;
		const value = globalThis.localStorage.getItem(key);
		if (value) {
			globalThis.localStorage.removeItem(key);
			return JSON.parse(value);
		}
		return null;
	}
	/**
	* @param {CallbackState} state
	*/
	add(state) {
		_assertClassBrand(_LocalStorage_brand, this, _clearInvalidValues).call(this);
		const key = STORAGE_KEY_PREFIX + state.state;
		const value = JSON.stringify(_objectSpread2(_objectSpread2({}, state), {}, { expires: Date.now() + 3600 * 1e3 }));
		try {
			globalThis.localStorage.setItem(key, value);
		} catch (error) {
			_assertClassBrand(_LocalStorage_brand, this, _clearAllValues).call(this);
			globalThis.localStorage.setItem(key, value);
		}
	}
};
/**
* Clears all values from local storage that are no longer valid.
*/
function _clearInvalidValues() {
	const currentTime = Date.now();
	for (const [key, value] of _assertClassBrand(_LocalStorage_brand, this, _getStoredEntries).call(this)) {
		const expiry = _assertClassBrand(_LocalStorage_brand, this, _parseExpiry).call(this, value);
		if (expiry === null || expiry < currentTime) globalThis.localStorage.removeItem(key);
	}
}
/**
* Clears all known values from local storage.
*/
function _clearAllValues() {
	for (const [key] of _assertClassBrand(_LocalStorage_brand, this, _getStoredEntries).call(this)) globalThis.localStorage.removeItem(key);
}
/**
* Gets all entries stored in local storage that are known to be managed by this class.
* @returns {[string, string][]} An array of key-value pairs.
*/
function _getStoredEntries() {
	return Object.entries(globalThis.localStorage).filter(([key]) => key.startsWith(STORAGE_KEY_PREFIX));
}
/**
* Parses the expiry time from a value stored in local storage.
* @param {string} value
* @returns {number | null} The expiry time in milliseconds, or `null` if the value is malformed.
*/
function _parseExpiry(value) {
	let parsedValue;
	try {
		parsedValue = JSON.parse(value);
	} catch (error) {
		return null;
	}
	if (isObject(parsedValue) && "expires" in parsedValue && typeof parsedValue.expires === "number") return parsedValue.expires;
	return null;
}
var _CookieStorage_brand = /* @__PURE__ */ new WeakSet();
/**
* @implements {CallbackStorage}
*/
var CookieStorage = class {
	constructor() {
		_classPrivateMethodInitSpec(this, _CookieStorage_brand);
	}
	/**
	* @param {string} [state]
	* @returns {CallbackState | null}
	*/
	get(state) {
		if (!state) return null;
		const value = _assertClassBrand(_CookieStorage_brand, this, _getCookie).call(this, STORAGE_KEY_PREFIX + state);
		_assertClassBrand(_CookieStorage_brand, this, _setCookie).call(this, STORAGE_KEY_PREFIX + state, "", _assertClassBrand(_CookieStorage_brand, this, _cookieExpiration).call(this, -100));
		if (value) return JSON.parse(value);
		return null;
	}
	/**
	* @param {CallbackState} state
	*/
	add(state) {
		_assertClassBrand(_CookieStorage_brand, this, _setCookie).call(this, STORAGE_KEY_PREFIX + state.state, JSON.stringify(state), _assertClassBrand(_CookieStorage_brand, this, _cookieExpiration).call(this, 60));
	}
};
/**
* @param {string} key
* @returns
*/
function _getCookie(key) {
	const name = key + "=";
	const ca = document.cookie.split(";");
	for (let i = 0; i < ca.length; i++) {
		let c = ca[i];
		while (c.charAt(0) === " ") c = c.substring(1);
		if (c.indexOf(name) === 0) return c.substring(name.length, c.length);
	}
	return "";
}
/**
* @param {string} key
* @param {string} value
* @param {Date} expirationDate
*/
function _setCookie(key, value, expirationDate) {
	const cookie = key + "=" + value + "; expires=" + expirationDate.toUTCString() + "; ";
	document.cookie = cookie;
}
/**
* @param {number} minutes
* @returns {Date}
*/
function _cookieExpiration(minutes) {
	const exp = /* @__PURE__ */ new Date();
	exp.setTime(exp.getTime() + minutes * 60 * 1e3);
	return exp;
}
/**
* @param {Uint8Array<ArrayBuffer>} bytes
* @see https://developer.mozilla.org/en-US/docs/Glossary/Base64#the_unicode_problem
*/
function bytesToBase64(bytes) {
	const binString = String.fromCodePoint(...bytes);
	return btoa(binString);
}
/**
* @param {string} message
* @see https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/digest#basic_example
*/
function sha256Digest(_x21) {
	return _sha256Digest.apply(this, arguments);
}
function _sha256Digest() {
	_sha256Digest = _asyncToGenerator(function* (message) {
		const data = new TextEncoder().encode(message);
		if (typeof crypto === "undefined" || typeof crypto.subtle === "undefined") throw new Error("Web Crypto API is not available.");
		return yield crypto.subtle.digest("SHA-256", data);
	});
	return _sha256Digest.apply(this, arguments);
}
/**
* @param {string} token
* @returns {KeycloakTokenParsed}
*/
function decodeToken(token) {
	const [, payload] = token.split(".");
	if (typeof payload !== "string") throw new Error("Unable to decode token, payload not found.");
	let decoded;
	try {
		decoded = base64UrlDecode(payload);
	} catch (error) {
		throw new Error("Unable to decode token, payload is not a valid Base64URL value.", { cause: error });
	}
	try {
		return JSON.parse(decoded);
	} catch (error) {
		throw new Error("Unable to decode token, payload is not a valid JSON value.", { cause: error });
	}
}
/**
* @param {string} input
*/
function base64UrlDecode(input) {
	let output = input.replaceAll("-", "+").replaceAll("_", "/");
	switch (output.length % 4) {
		case 0: break;
		case 2:
			output += "==";
			break;
		case 3:
			output += "=";
			break;
		default: throw new Error("Input is not of the correct length.");
	}
	try {
		return b64DecodeUnicode(output);
	} catch (error) {
		return atob(output);
	}
}
/**
* @param {string} input
*/
function b64DecodeUnicode(input) {
	return decodeURIComponent(atob(input).replace(/(.)/g, (m, p) => {
		let code = p.charCodeAt(0).toString(16).toUpperCase();
		if (code.length < 2) code = "0" + code;
		return "%" + code;
	}));
}
/**
* Check if the input is an object that can be operated on.
* @param {unknown} input
*/
function isObject(input) {
	return typeof input === "object" && input !== null;
}
/**
* @typedef {Object} JsonConfig The JSON version of the adapter configuration.
* @property {string} auth-server-url The URL of the authentication server.
* @property {string} realm The name of the realm.
* @property {string} resource The name of the resource, usually the client ID.
*/
/**
* Fetch the adapter configuration from the given URL.
* @param {string} url
* @returns {Promise<JsonConfig>}
*/
function fetchJsonConfig(_x22) {
	return _fetchJsonConfig.apply(this, arguments);
}
function _fetchJsonConfig() {
	_fetchJsonConfig = _asyncToGenerator(function* (url) {
		return yield fetchJSON(url);
	});
	return _fetchJsonConfig.apply(this, arguments);
}
/**
* Fetch the OpenID configuration from the given URL.
* @param {string} url
* @returns {Promise<OpenIdProviderMetadata>}
*/
function fetchOpenIdConfig(_x23) {
	return _fetchOpenIdConfig.apply(this, arguments);
}
function _fetchOpenIdConfig() {
	_fetchOpenIdConfig = _asyncToGenerator(function* (url) {
		return yield fetchJSON(url);
	});
	return _fetchOpenIdConfig.apply(this, arguments);
}
/**
* @typedef {Object} AccessTokenResponse The successful token response from the authorization server, based on the {@link https://datatracker.ietf.org/doc/html/rfc6749#section-5.1 OAuth 2.0 Authorization Framework specification}.
* @property {string} access_token The access token issued by the authorization server.
* @property {string} token_type The type of the token issued by the authorization server.
* @property {number} [expires_in] The lifetime in seconds of the access token.
* @property {string} [refresh_token] The refresh token issued by the authorization server.
* @property {string} [id_token] The ID token issued by the authorization server, if requested.
* @property {string} [scope] The scope of the access token.
*/
/**
* Fetch the access token from the given URL.
* @param {string} url
* @param {string} code
* @param {string} clientId
* @param {string} redirectUri
* @param {string} [pkceCodeVerifier]
* @returns {Promise<AccessTokenResponse>}
*/
function fetchAccessToken(_x24, _x25, _x26, _x27, _x28) {
	return _fetchAccessToken.apply(this, arguments);
}
function _fetchAccessToken() {
	_fetchAccessToken = _asyncToGenerator(function* (url, code, clientId, redirectUri, pkceCodeVerifier) {
		const body = new URLSearchParams([
			["code", code],
			["grant_type", "authorization_code"],
			["client_id", clientId],
			["redirect_uri", redirectUri]
		]);
		if (pkceCodeVerifier) body.append("code_verifier", pkceCodeVerifier);
		return yield fetchJSON(url, {
			method: "POST",
			credentials: "include",
			body
		});
	});
	return _fetchAccessToken.apply(this, arguments);
}
/**
* Fetch the refresh token from the given URL.
* @param {string} url
* @param {string} refreshToken
* @param {string} clientId
* @returns {Promise<AccessTokenResponse>}
*/
function fetchRefreshToken(_x29, _x30, _x31) {
	return _fetchRefreshToken.apply(this, arguments);
}
function _fetchRefreshToken() {
	_fetchRefreshToken = _asyncToGenerator(function* (url, refreshToken, clientId) {
		return yield fetchJSON(url, {
			method: "POST",
			credentials: "include",
			body: new URLSearchParams([
				["grant_type", "refresh_token"],
				["refresh_token", refreshToken],
				["client_id", clientId]
			])
		});
	});
	return _fetchRefreshToken.apply(this, arguments);
}
/**
* @template [T=unknown]
* @param {string} url
* @param {RequestInit} init
* @returns {Promise<T>}
*/
function fetchJSON(_x32) {
	return _fetchJSON.apply(this, arguments);
}
function _fetchJSON() {
	_fetchJSON = _asyncToGenerator(function* (url, init = {}) {
		const headers = new Headers(init.headers);
		headers.set("Accept", CONTENT_TYPE_JSON);
		return yield (yield fetchWithErrorHandling(url, _objectSpread2(_objectSpread2({}, init), {}, { headers }))).json();
	});
	return _fetchJSON.apply(this, arguments);
}
/**
* @param {string} url
* @param {RequestInit} [init]
* @returns {Promise<Response>}
*/
function fetchWithErrorHandling(_x33, _x34) {
	return _fetchWithErrorHandling.apply(this, arguments);
}
function _fetchWithErrorHandling() {
	_fetchWithErrorHandling = _asyncToGenerator(function* (url, init) {
		const response = yield fetch(url, init);
		if (!response.ok) throw new NetworkError("Server responded with an invalid status.", { response });
		return response;
	});
	return _fetchWithErrorHandling.apply(this, arguments);
}
/**
* @param {string} [token]
* @returns {[string, string]}
*/
function buildAuthorizationHeader(token) {
	if (!token) throw new Error("Unable to build authorization header, token is not set, make sure the user is authenticated.");
	return ["Authorization", `bearer ${token}`];
}
/**
* @param {string} url
* @returns {string}
*/
function stripTrailingSlash(url) {
	return url.endsWith("/") ? url.slice(0, -1) : url;
}
/**
* @typedef {Object} NetworkErrorOptionsProperties
* @property {Response} response
* @typedef {ErrorOptions & NetworkErrorOptionsProperties} NetworkErrorOptions
*/
var NetworkError = class extends Error {
	/**
	* @param {string} message
	* @param {NetworkErrorOptions} options
	*/
	constructor(message, options) {
		super(message, options);
		_defineProperty(
			this,
			/** @type {Response} */
			"response",
			void 0
		);
		this.response = options.response;
	}
};
/**
* @param {number} delay
* @returns {Promise<void>}
*/
var waitForTimeout = (delay) => new Promise((resolve) => setTimeout(resolve, delay));
//#endregion
export { NetworkError, Keycloak as default };
