export type UserType = 'ADMIN' | 'STAFF' | 'CUSTOMER';

export type AuthUser = {
  id: number;
  username: string;
  email?: string;
  phoneNumber?: string;
  userType: UserType;
  roles: string[];
  permissions: string[];
};
