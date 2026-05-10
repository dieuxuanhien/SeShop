import { Mail, RotateCcw, Truck } from 'lucide-react';
import { Card } from '@/shared/ui/Card';

const pageContent = {
  contact: {
    title: 'Contact Us',
    icon: Mail,
    sections: [
      ['Client Care', 'support@seshop.vn'],
      ['Showroom', '12 Le Loi, District 1, Ho Chi Minh City'],
      ['Hours', '10:00 - 21:00 daily'],
    ],
  },
  shipping: {
    title: 'Shipping & Returns',
    icon: Truck,
    sections: [
      ['Domestic Shipping', 'Standard delivery is 3 to 5 business days. Express delivery is available in Ho Chi Minh City.'],
      ['Returns', 'Returns are accepted within 30 days for eligible items in original condition.'],
      ['Refunds', 'Approved refunds are returned to the original payment method or store credit.'],
    ],
  },
  faq: {
    title: 'FAQ',
    icon: RotateCcw,
    sections: [
      ['Are pieces authentic?', 'Each vintage item is inspected before publishing.'],
      ['Can I reserve an item?', 'Carted pieces are not reserved until checkout is complete.'],
      ['Can I check store stock?', 'Use the availability link on any product detail page.'],
    ],
  },
} as const;

type InfoPageProps = {
  type: keyof typeof pageContent;
};

export function InfoPage({ type }: InfoPageProps) {
  const content = pageContent[type];
  const Icon = content.icon;

  return (
    <div className="mx-auto max-w-4xl px-6 py-20 lg:px-12">
      <div className="mb-10 text-center">
        <Icon className="mx-auto mb-4 text-primary" size={34} strokeWidth={1.5} />
        <h1 className="font-display text-4xl text-highlight">{content.title}</h1>
      </div>
      <Card className="border-primary/20 bg-surface/95 p-6">
        <div className="grid gap-4">
          {content.sections.map(([heading, body]) => (
            <section key={heading} className="rounded-md border border-primary/15 bg-ink/[0.03] p-4">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">{heading}</h2>
              <p className="mt-2 text-sm leading-6 text-ink/65">{body}</p>
            </section>
          ))}
        </div>
      </Card>
    </div>
  );
}
