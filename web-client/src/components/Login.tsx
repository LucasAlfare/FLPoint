import { Stack, TextField, Button } from '@mui/material';
import React, { useState } from 'react';

const Login: React.FC = () => {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState<string>("");

  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(event.target.value);
  };

  return (
    <div>
      <Stack direction='column'>
        <h1>Cadastre-se</h1>
        <Stack direction='row'>
          <TextField onChange={handleEmailChange} label="e-mail" variant="standard"></TextField>
        </Stack>

        <Stack direction='row'>
          <TextField onChange={handlePasswordChange} label="senha" variant="standard" type="password"></TextField>
        </Stack>

        <Button onClick={() => {
          // console.log(`${email} / ${password}`);

          // TODO: send request to signup

        }} variant="contained">Entrar</Button>
      </Stack>
    </div>
  )
}

export default Login