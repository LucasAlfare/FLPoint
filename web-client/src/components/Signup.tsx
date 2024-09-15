import { Stack, TextField, Button } from '@mui/material';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Signup: React.FC = () => {

  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState<string>("");

  const handleNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setName(event.target.value);
  };

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
          <TextField onChange={handleNameChange} label="nome" variant="standard"></TextField>
        </Stack>

        <Stack direction='row'>
          <TextField onChange={handleEmailChange} label="e-mail" variant="standard"></TextField>
        </Stack>

        <Stack direction='row'>
          <TextField onChange={handlePasswordChange} label="senha" variant="standard" type="password"></TextField>
        </Stack>

        <Button onClick={() => {
          // console.log(`${name} / ${email} / ${password}`);

          // TODO: send request to signup
          // navigate('/')
        }} variant="contained">Cadastrar</Button>
      </Stack>
    </div>
  )
}
export default Signup