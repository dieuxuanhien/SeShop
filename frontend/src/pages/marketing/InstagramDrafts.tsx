import { PageScaffold } from '@/shared/ui/PageScaffold';

export function InstagramDrafts() {
  return (
    <PageScaffold
      title="Instagram Compose & Draft Management"
      viewCode="STAFF_009"
      purpose="Manual-review Instagram draft workflow with product media, caption, hashtags, media ordering, and approval."
      endpoints={['POST /marketing/drafts', 'PUT /marketing/drafts/{draftId}', 'POST /marketing/drafts/{draftId}/submit-review', 'POST /marketing/drafts/{draftId}/approve']}
    />
  );
}
