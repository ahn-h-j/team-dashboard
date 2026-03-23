import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  createElement,
  type ReactNode,
} from 'react';
import {
  type User,
  type AuthTokens,
  getAccessToken,
  getRefreshToken,
  saveTokens,
  clearTokens,
  decodeJwtPayload,
} from '../lib/auth';
import { api } from '../lib/api';

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, name: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getAccessToken();
    if (token) {
      const decoded = decodeJwtPayload(token);
      setUser(decoded);
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const tokens = await api.post<AuthTokens>('/api/auth/login', {
      email,
      password,
    });
    saveTokens(tokens);
    const decoded = decodeJwtPayload(tokens.accessToken);
    setUser(decoded);
  }, []);

  const signup = useCallback(
    async (email: string, password: string, name: string) => {
      const tokens = await api.post<AuthTokens>('/api/auth/signup', {
        email,
        password,
        name,
      });
      saveTokens(tokens);
      const decoded = decodeJwtPayload(tokens.accessToken);
      setUser(decoded);
    },
    [],
  );

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken();
    try {
      if (refreshToken) {
        await api.post('/api/auth/logout', { refreshToken });
      }
    } catch {
      // Ignore logout API errors
    } finally {
      clearTokens();
      setUser(null);
    }
  }, []);

  return createElement(
    AuthContext.Provider,
    {
      value: {
        user,
        isAuthenticated: user !== null,
        isLoading,
        login,
        signup,
        logout,
      },
    },
    children,
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
