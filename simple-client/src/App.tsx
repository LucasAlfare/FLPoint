/**
 * This is the saddest frontend ever
 */
import { Button, Divider, List, ListItem, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { Route, Routes, BrowserRouter as Router, useNavigate, useLocation } from "react-router-dom";

const serverUrl = "http://localhost:7171";
const loginEndpoint = "/login";
const createPointEndPoint = "/user/point";
const getAllOwnUserPointsEndpoint = "/user/points";
const changePassowordEndpoint = "/user/update-password";
const userLogoutEndpoint = "/user/logout";
const getAllUsersEndpoint = "/admin/users";

// types for matching what exists in the Kotlin API; handful
type LocalTime = string;
type TimeZone = string;
type Instant = string;

interface TimeInterval {
  enter: LocalTime;
  exit: LocalTime;
}

// interface CreateUserRequestDTO {
//   name: string;
//   email: string;
//   plainPassword: string;
//   timeIntervals: TimeInterval[];
//   timeZone?: TimeZone;
// }

interface LoginResponseDTO {
  jwt: string;
  userDTO: UserDTO;
}

interface UserDTO {
  id: number;
  name: string;
  email: string;
  timeIntervals: TimeInterval[];
  timeZone: TimeZone,
  isAdmin: boolean;
}

interface PointDTO {
  id: number;
  relatedUserId: number;
  instant: Instant;
}

function getCookie(name: string): string | null {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
}

function setCookie(name: string, value: string) {
  document.cookie = `${name}=${value}; max-age=3600; path=/; secure; samesite=strict`;
}

function Login() {

  const [email, setEmail] = useState<string>("");
  const [plainPassword, setPlainPassword] = useState<string>("");
  const navigate = useNavigate();

  async function handleLogin() {
    const credentials = {
      email: email,
      plainPassword: plainPassword
    };

    try {
      const response = await fetch(`${serverUrl}${loginEndpoint}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials)
      });

      if (!response.ok) {
        const t = await response.text();
        throw new Error(response.status + "\n" + t);
      }

      const loginResponse: LoginResponseDTO = await response.json();

      // if login is ok, then stores the JWT in cookies
      setCookie('jwt', loginResponse.jwt);

      const nextPanelRoute = loginResponse.userDTO.isAdmin ? "/admin-panel" : "/user-panel";
      navigate(nextPanelRoute, { state: loginResponse.userDTO });
    } catch (error) {
      alert(error);
    }
  }

  return (
    <div>
      <Stack direction={'column'} spacing={2}>
        <Typography variant="h3">Faça o login</Typography>

        <TextField onChange={(e) => {
          setEmail(e.target.value);
        }} label={"Email"}></TextField>

        <TextField type="password" onChange={(e) => {
          setPlainPassword(e.target.value);
        }} label={"Senha"}></TextField>

        <Button variant={'contained'} onClick={() => {
          handleLogin();
        }}>Login</Button>
      </Stack>
    </div>
  )
}

interface PointRegistryProps {
  instantStr: string;
  timeZone: TimeZone
}

function PointRegistry({ instantStr, timeZone }: PointRegistryProps) {
  function formatDateTime(dateString: string, timeZone: string) {
    const date = new Date(dateString);
    const formattedDate = date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      timeZone: timeZone,
    });
    const formattedTime = date.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      timeZone: timeZone,
    });
    return `${formattedDate}, ${formattedTime}`;
  }

  return (
    <div>
      <p>- {formatDateTime(instantStr, timeZone)}</p>
    </div>
  );
}

function ChangePasswordPanel() {
  const navigate = useNavigate();

  const [currentPass, setCurrentPass] = useState<string>("");
  const [newPass, setNewPass] = useState<string>("");

  async function handleChangePassword() {
    const b = {
      currentPlainPassword: currentPass,
      newPlainPassword: newPass
    };

    try {
      const changePasswordResponse = await fetch(`${serverUrl}${changePassowordEndpoint}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${getCookie("jwt")}`
        },
        body: JSON.stringify(b)
      })

      if (!changePasswordResponse.ok) {
        const t = await changePasswordResponse.text();
        throw new Error(changePasswordResponse.status + "\n" + t);
      }

      alert("Senha alterada com sucesso!");
      navigate(-1); // back to previous page
    } catch (error) {
      alert(error);
    }
  }

  return (
    <div>
      <Stack direction={'column'} spacing={2}>
        <Typography variant="h3">Alterar senha</Typography>
        <Typography>Para alterar a senha, digite a senha atual e a nova senha nos campos abaixo:</Typography>
        <TextField type="password" label={"Senha atual"} onChange={(e) => { setCurrentPass(e.target.value) }}></TextField>
        <TextField type="password" label={"Nova Senha"} onChange={(e) => { setNewPass(e.target.value) }}></TextField>
        <Button variant={'contained'} onClick={() => { handleChangePassword() }}>Alterar</Button>
      </Stack>
    </div>
  )
}

function StandardUserPanel() {

  const navigate = useNavigate();
  const self = useLocation().state as UserDTO;
  const [points, setPoints] = useState<Array<PointDTO>>([]);
  const [registeringPoint, setRegisteringPoint] = useState<boolean>(false);

  async function handleGetAllPoints() {
    try {
      const response = await fetch(`${serverUrl}${getAllOwnUserPointsEndpoint}`, {
        headers: {
          "Authorization": `Bearer ${getCookie("jwt")}`
        }
      });

      if (!response.ok) {
        throw new Error("Erro ao obter todos os pontos do usuário: " + response.status);
      }

      const allPointsResponse: Array<PointDTO> = await response.json();
      setPoints(allPointsResponse);
    } catch (error) {
      console.error("Erro:", error);
    }
  }

  async function handleCreatePoint() {
    setRegisteringPoint(true);
    try {
      const createPointResponse = await fetch(`${serverUrl}${createPointEndPoint}`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${getCookie("jwt")}`
        }
      })

      if (!createPointResponse.ok) {
        const message = await createPointResponse.text();
        throw new Error(createPointResponse.status + "\n" + message);
      }

      console.log("Ponto registrado com sucesso!");
      handleGetAllPoints();
      setRegisteringPoint(false);
    } catch (error) {
      console.error(error);
      setRegisteringPoint(false);
      alert(error);
    }
  }

  async function handleChangePassword() {
    navigate("/change-password");
  }

  function LoadedPoints() {
    if (points.length !== 0) {
      return (<div>
        <Typography>Seus registros de ponto:</Typography>
        <List>
          {points.map(p => {
            return (
              <ListItem key={p.id}>
                <PointRegistry instantStr={p.instant} timeZone={self.timeZone}></PointRegistry>
              </ListItem>
            )
          })}
        </List>
      </div>);
    } else {
      return <Typography>Nenhum registro de ponto ainda!</Typography>
    }
  }

  async function handleLogout() {
    const currentJwt = getCookie('jwt');

    try {
      const logoutResponse = await fetch(`${serverUrl}${userLogoutEndpoint}`, {
        method: 'POST',
        headers: {
          "Authorization": `Bearer ${currentJwt}`,
          "Content-Type": "application/json", // needed when sendin raw string?
        },
        body: currentJwt
      });

      if (!logoutResponse.ok) {
        throw new Error(logoutResponse.status + "\n" + "Não é possível fazer logout!?!?!?!?");
      }

      setCookie('jwt', '');
      navigate('/');
    } catch (error) {
      alert(error);
    }
  }

  return (
    <Stack direction={'column'} spacing={1}>
      <Stack direction={'row'} spacing={1}>
        <Typography>Bem-vindo</Typography>
        <Typography variant="h4">{self.name}</Typography>
      </Stack>

      <Typography>Seus horários:</Typography>
      <List>
        {self.timeIntervals.map(t => {
          return <ListItem key={`${self.id}-${Math.random()}`}>
            <Stack direction={'row'} spacing={1}>
              <Typography>Entrada: {t.enter};</Typography>
              <Divider orientation="vertical" />
              <Typography>Saída: {t.exit};</Typography>
            </Stack>
            <Divider />
          </ListItem>
        })}
      </List>

      <Stack direction={'column'} spacing={1}>
        <Button variant="contained" onClick={() => { handleGetAllPoints() }}>Carregar registros de Ponto</Button>
        <Button variant="contained" disabled={registeringPoint} onClick={() => { handleCreatePoint() }}>Registrar Ponto</Button>
        <Button variant="contained" onClick={() => { handleChangePassword() }}>Alterar Senha</Button>
      </Stack>

      <LoadedPoints />
      <Button variant="contained" onClick={() => { handleLogout() }}>Sair</Button>
    </Stack>
  )
}

interface UserViewProps {
  user: UserDTO
}

function UserView({ user }: UserViewProps) {

  // TODO: consider abstract to be used outside
  function DecoredUserName() {
    if (user.isAdmin) {
      return (
        <Typography>Nome: {user.name} 👑</Typography>
      )
    } else {
      return (
        <Typography>Nome: {user.name}</Typography>
      )
    }
  }

  return (
    <div>
      <Stack direction={'column'} spacing={1}>
        <DecoredUserName />
        <Typography>Email: {user.email}</Typography>
        <Stack direction={'column'} spacing={1}>
          <List>
            {
              user.timeIntervals.map(
                t => {
                  return (
                    <ListItem key={`${user.id}-${Math.random()}`}>
                      <Stack direction={'row'} spacing={1}>
                        <Typography>Entrada: {t.enter}</Typography>
                        <Divider orientation="vertical" />
                        <Typography>Saída: {t.exit}</Typography>
                      </Stack>
                    </ListItem>
                  )
                }
              )
            }
          </List>
        </Stack>
      </Stack>
    </div>
  );
}

function AdminPanel() {
  const navigate = useNavigate();

  const self = useLocation().state as UserDTO;

  const [allUsers, setAllUsers] = useState<UserDTO[]>([]);

  async function handleGetAllUsers() {
    try {
      const allUsersResponse = await fetch(`${serverUrl}${getAllUsersEndpoint}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${getCookie("jwt")}`
        }
      });

      if (!allUsersResponse.ok) {
        const t = await allUsersResponse.text();
        throw new Error(allUsersResponse.status + "\n" + t);
      }

      const data: UserDTO[] = await allUsersResponse.json();
      setAllUsers(data);
    } catch (error) {
      alert(error);
    }
  }

  function AllUsersList() {
    if (allUsers.length === 0) {
      return (<Typography>Nenhum usuário foi recebido do servidor. Tente carregar novamente.</Typography>);
    }

    return (
      <div>
        <List>
          {allUsers.map(u => {
            return (
              <ListItem key={u.id}>
                <UserView user={u} />
                <Divider />
              </ListItem>
            )
          })}
        </List>
      </div>
    );
  }

  function handleLogout() {
    setCookie('jwt', '');
    navigate('/');
  }

  return (
    <div>
      <Stack direction={'column'}>
        <Stack direction={'row'} spacing={1}>
          <Typography>Bem-vindo</Typography>
          <Stack direction={'row'} spacing={1}>
            <Typography variant="h3">{self.name}</Typography>
            <Typography variant="h3">👑</Typography>
          </Stack>
        </Stack>

        <Stack direction={'column'} spacing={1}>
          <Button variant="contained" onClick={() => { handleGetAllUsers() }}>Carregar usuários</Button>
        </Stack>

        <AllUsersList />

        <Button variant="contained" onClick={() => { handleLogout() }}>Sair</Button>
      </Stack>
    </div>
  )
}

function Home() {
  const navigate = useNavigate();

  return (
    <Stack direction={'column'}>
      <Typography variant="h1">Bem-vindo!</Typography>
      <Button variant="contained" onClick={() => { navigate("/login") }}>Fazer Login</Button>
    </Stack>
  )
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/user-panel" element={<StandardUserPanel />} />
        <Route path="/change-password" element={<ChangePasswordPanel />} />
        <Route path="/admin-panel" element={<AdminPanel />} />
      </Routes>
    </Router>
  )
}

export default App