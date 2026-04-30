export type UserType = 'ADMIN' | 'STAFF' | 'CUSTOMER';

export type AuthUser = {
  id: number;
  username: string;
  userType: UserType;
  roles: string[];
  permissions: string[];
};
