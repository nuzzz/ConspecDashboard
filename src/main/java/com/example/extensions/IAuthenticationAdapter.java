package com.example.extensions;

import com.microsoft.graph.authentication.IAuthenticationProvider;

//------------------------------------------------------------------------------
//Copyright (c) Microsoft Corporation.  All Rights Reserved.  Licensed under the MIT License.  See License in the project root for license information.
//------------------------------------------------------------------------------


import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;

/**
* An authentication adapter for signing requests, logging in, and logging out.
*/
public interface IAuthenticationAdapter extends IAuthenticationProvider  {

/**
 * Logs out the user
 *
 * @param callback The callback when the logout is complete or an error occurs
 */
void logout(final ICallback<Void> callback);

/**
 * Login a user with no ui
 *
 * @param callback The callback when the login is complete or an error occurs
 */
void loginSilent(final ICallback<Void> callback);

/**
 * Gets the access token for the session of a logged in user
 *
 * @return the access token
 */
String getAccessToken() throws ClientException;
}

