import axios from 'axios';

const API = axios.create({
  baseURL: 'http://192.168.0.16:8080/auth',
});

API.interceptors.request.use((config) => {
  localStorage.getItem('jwt');
  return config;
});

export default API;

export async function signIn(values) {
  const response = await API.post('/signin', values);

  const token = response.data.token;
  localStorage.setItem('jwt', token);
  return token;
}

export async function signUp(values) {
  const response = await API.post('/signup', values);

  const token = response.data.token;
  localStorage.setItem('jwt', token);
  return token;
}


