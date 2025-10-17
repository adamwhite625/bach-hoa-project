import axios from "./axios.customize";
import { API_ENDPOINTS } from "../config/api";

const createUserApi = (fullName, email, password, phone) => {
  const URL = API_ENDPOINTS.AUTH.REGISTER;

  const data = {
    fullName,
    email,
    password,
    phone
  };

  return axios.post(URL, data);
}

const handleLogin = (email, password) => {
  const URL = API_ENDPOINTS.AUTH.LOGIN;

  const data = {
    email,
    password
  };

  console.log("Login data:", data); // Debug log

  return axios.post(URL, data);
}

const forgotPasswordApi = (email) => {
  const URL = API_ENDPOINTS.AUTH.FORGOT_PASSWORD;

  const data = {
    email,
  };

  return axios.post(URL, data);
}

const resetPasswordApi = (token, newPassword) => {
  const base = API_ENDPOINTS.AUTH.RESET_PASSWORD;
  const URL = `${base}/${token}`;

  const data = {
    token,
    newPassword,
  };

  return axios.post(URL, data);
}

const getAccount = (token) => {
  const URL = API_ENDPOINTS.USERS.PROFILE;

  return axios.get(URL, token);
}

export { createUserApi, handleLogin, forgotPasswordApi, resetPasswordApi, getAccount };