import { createContext, useContext, useState, ReactNode } from 'react';
import { authService, AuthResponse } from '@/services/authService';

interface AuthContextType {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isRoot: boolean;
  userId: string | null;
  roles: string[];
  login: (authResponse: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  
  // --- HELPER FUNCTIONS ---
  const parseRoles = (storedRoles: string | null): string[] => {
    if (!storedRoles) return [];
    try {
      return JSON.parse(storedRoles);
    } catch (e) {
      return [storedRoles]; // Handle raw string case
    }
  };

  const checkIsAdmin = (roleList: string[]) => {
    return roleList.some(r => {
      const role = String(r).toUpperCase(); // Force string conversion
      return role.includes('ADMIN') || role.includes('ROOT');
    });
  };

  const checkIsRoot = (roleList: string[]) => {
    return roleList.some(r => String(r).toUpperCase().includes('ROOT'));
  };

  // --- LAZY STATE INITIALIZATION (The Fix) ---
  // This runs BEFORE the page renders, preventing the "Customer Dashboard" flash.

  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return !!localStorage.getItem('accessToken');
  });

  const [userId, setUserId] = useState(() => {
    return localStorage.getItem('userId');
  });

  const [roles, setRoles] = useState<string[]>(() => {
    return parseRoles(localStorage.getItem('userRoles'));
  });

  const [isAdmin, setIsAdmin] = useState(() => {
    const r = parseRoles(localStorage.getItem('userRoles'));
    return checkIsAdmin(r);
  });

  const [isRoot, setIsRoot] = useState(() => {
    const r = parseRoles(localStorage.getItem('userRoles'));
    return checkIsRoot(r);
  });

  // --- LOGIN / LOGOUT HANDLERS ---

  const login = (authResponse: AuthResponse) => {
    authService.setAuthData(authResponse);

    const safeRoles = Array.isArray(authResponse.roles) 
      ? authResponse.roles 
      : [authResponse.roles as string];

    setIsAuthenticated(true);
    setUserId(authResponse.ownerUserId);
    setRoles(safeRoles);
    setIsAdmin(checkIsAdmin(safeRoles));
    setIsRoot(checkIsRoot(safeRoles));
  };

  const logout = () => {
    authService.clearAuthData();
    setIsAuthenticated(false);
    setIsAdmin(false);
    setIsRoot(false);
    setUserId(null);
    setRoles([]);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, isAdmin, isRoot, userId, roles, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};