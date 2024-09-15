import { Stack, Button } from '@mui/material';
import React from "react";
import { useNavigate } from 'react-router-dom';

const Home: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Stack direction='column'>
      <Button variant='contained' onClick={() => {
        navigate('/signup')
      }}>Ir para cadastro</Button>

      <Button variant='contained' onClick={() => {
        navigate('/login')
      }}>Fazer login</Button>
    </Stack>
  )
}

export default Home;