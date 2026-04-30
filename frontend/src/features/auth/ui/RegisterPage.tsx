import { zodResolver } from '@hookform/resolvers/zod';
import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { register as registerAccount } from '../api/authApi';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Spinner } from '@/shared/ui/Spinner';
import type { ApiError } from '@/shared/types/api';

const schema = z.object({
  username: z.string().min(2, 'Username must be at least 2 characters'),
  email: z.string().email('Enter a valid email'),
  phoneNumber: z.string().min(8, 'Enter a valid phone number'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

type RegisterFormValues = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(values: RegisterFormValues) {
    setSubmitError(null);
    try {
      await registerAccount(values);
      navigate('/auth/login', { replace: true });
    } catch (error) {
      setSubmitError((error as ApiError).message ?? 'Registration failed');
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-surface px-4 py-8">
      <Card className="w-full max-w-lg p-6">
        <div className="mb-6">
          <p className="text-xs font-semibold uppercase tracking-wide text-primary">SeShop</p>
          <h1 className="mt-1 text-2xl font-semibold text-ink">Create account</h1>
        </div>

        <form className="grid gap-4" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Username" autoComplete="username" error={errors.username?.message} {...register('username')} />
          <Input label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
          <Input label="Phone number" autoComplete="tel" error={errors.phoneNumber?.message} {...register('phoneNumber')} />
          <Input label="Password" type="password" autoComplete="new-password" error={errors.password?.message} {...register('password')} />

          {submitError ? <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-danger">{submitError}</p> : null}

          <Button type="submit" disabled={isSubmitting} icon={isSubmitting ? <Spinner /> : <UserPlus size={16} />}>
            Create account
          </Button>
        </form>

        <p className="mt-4 text-sm text-slate-600">
          Already registered?{' '}
          <Link className="font-medium text-primary" to="/auth/login">
            Sign in
          </Link>
        </p>
      </Card>
    </main>
  );
}
