import { zodResolver } from '@hookform/resolvers/zod';
import { LogIn } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { login } from '../api/authApi';
import { useAuthStore } from '../model/authStore';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Spinner } from '@/shared/ui/Spinner';
import type { ApiError } from '@/shared/types/api';

const schema = z.object({
  usernameOrEmail: z.string().min(1, 'Username or email is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      usernameOrEmail: '',
      password: '',
    },
  });

  async function onSubmit(values: LoginFormValues) {
    setSubmitError(null);
    try {
      const result = await login(values);
      setAuth(result.accessToken, result.user);
      const from = (location.state as { from?: Location } | null)?.from?.pathname ?? '/';
      navigate(from, { replace: true });
    } catch (error) {
      setSubmitError((error as ApiError).message ?? 'Login failed');
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-surface px-4 py-8">
      <Card className="w-full max-w-md p-6">
        <div className="mb-6">
          <p className="text-xs font-semibold uppercase tracking-wide text-primary">SeShop</p>
          <h1 className="mt-1 text-2xl font-semibold text-ink">Sign in</h1>
        </div>

        <form className="grid gap-4" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Username or email" autoComplete="username" error={errors.usernameOrEmail?.message} {...register('usernameOrEmail')} />
          <Input label="Password" type="password" autoComplete="current-password" error={errors.password?.message} {...register('password')} />

          {submitError ? <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-danger">{submitError}</p> : null}

          <Button type="submit" disabled={isSubmitting} icon={isSubmitting ? <Spinner /> : <LogIn size={16} />}>
            Sign in
          </Button>
        </form>

        <p className="mt-4 text-sm text-slate-600">
          New customer?{' '}
          <Link className="font-medium text-primary" to="/auth/register">
            Create an account
          </Link>
        </p>
      </Card>
    </main>
  );
}
