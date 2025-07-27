  const cards = document.querySelectorAll('.card-selectable');
  const pickLinkBtn = document.getElementById('pick-link-btn');

  cards.forEach(card => {
    card.addEventListener('click', () => {
      cards.forEach(c => c.classList.remove('card-selected'));
      card.classList.add('card-selected');
      pickLinkBtn.href = `/book?sid=${card.dataset.serviceId}`;
      pickLinkBtn.classList.remove('disabled');
    });
  });