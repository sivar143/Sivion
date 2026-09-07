import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <main class="shell">
      <header><div><span class="eyebrow">SIVION</span><h1>Business command center</h1><p>CRM, inventory and order operations in one workspace.</p></div><button>New order</button></header>
      <section class="cards">
        <article><span>Revenue</span><strong>₹48.5L</strong><small>+12.4% this month</small></article>
        <article><span>Orders</span><strong>1,284</strong><small>86 awaiting fulfillment</small></article>
        <article><span>Customers</span><strong>3,842</strong><small>142 new this month</small></article>
        <article><span>Low stock</span><strong>27</strong><small>Items need attention</small></article>
      </section>
      <section class="grid"><article class="panel"><h2>Order pipeline</h2><div class="row"><span>Confirmed</span><b>48</b></div><div class="row"><span>Picking</span><b>31</b></div><div class="row"><span>Packing</span><b>19</b></div><div class="row"><span>Ready to ship</span><b>12</b></div></article><article class="panel"><h2>Quick actions</h2><div class="actions"><button>Customers</button><button>Products</button><button>Inventory</button><button>Purchase order</button></div></article></section>
    </main>`
})
export class AppComponent {}
