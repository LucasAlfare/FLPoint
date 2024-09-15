import { Typography, List, ListItem, Stack } from '@mui/material';
import React, { useState } from 'react';


const Points: React.FC = () => {

  const [points, setPoints] = useState<string[]>(['[vazio]']);

  return (
    <div>
      <Stack>
        <h1>Todos os pontos:</h1>
        <List>
          {
            points.map(p => {
              return (
                <ListItem>
                  <Typography>{p}</Typography>
                </ListItem>
              )
            })
          }
        </List>
      </Stack>
    </div>
  )
}

export default Points