import { Stack, Typography, Button } from '@mui/material';
import React from "react";
import { useNavigate } from 'react-router-dom';

const CreatePoint: React.FC = () => {

  const navigate = useNavigate();

  return (
    <Stack>
      <Typography>Clique no botão abaixo para fazer um registro de ponto</Typography>
      <Button variant='contained' onClick={() => {

      }}>Registrar ponto!</Button>

      <Button onClick={() => {
        navigate('/points')
      }}>Ver seus pontos</Button>
    </Stack>
  )
}

export default CreatePoint